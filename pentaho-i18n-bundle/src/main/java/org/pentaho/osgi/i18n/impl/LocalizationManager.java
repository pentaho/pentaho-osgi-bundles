/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.i18n.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundle;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleCacheCallable;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/4/14.
 */
public class LocalizationManager implements LocalizationService {
  private static Logger log = LoggerFactory.getLogger( LocalizationManager.class );
  private final Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap =
    new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>();
  private final JSONParser parser = new JSONParser();
  private ExecutorService executorService;
  private volatile Future<Map<String, Map<String, OSGIResourceBundle>>> cache;

  // For unit tests only
  static Logger getLog() {
    return log;
  }

  // For unit tests only
  static void setLog( Logger log ) {
    LocalizationManager.log = log;
  }

  public void setExecutorService( ExecutorService executorService ) {
    this.executorService = executorService;
  }

  public void bundleChanged( Bundle bundle ) throws IOException, ParseException {
    boolean rebuildCache;
    synchronized ( configMap ) {
      rebuildCache = configMap.remove( bundle.getBundleId() ) != null;
    }
    JSONObject bundleConfig = loadJsonObject( bundle.getResource( "META-INF/js/i18n.json" ) );
    if ( bundleConfig != null ) {
      Map<String, List<OSGIResourceBundleFactory>> configEntry = new HashMap<String, List<OSGIResourceBundleFactory>>();
      for ( Map.Entry<Object, Object> entry : ( (Map<Object, Object>) bundleConfig ).entrySet() ) {
        String key = entry.getKey().toString();
        if ( entry.getValue() != null ) {
          List<Object> names = (List<Object>) entry.getValue();
          List<OSGIResourceBundleFactory> bundles = new ArrayList<OSGIResourceBundleFactory>();
          for ( Object nameObj : names ) {
            String name;
            int priority = 0;
            if ( nameObj instanceof String ) {
              name = (String) nameObj;
            } else {
              Map<String, Object> nameMap = (Map<String, Object>) nameObj;
              name = (String) nameMap.get( "name" );
              Object priorityObj = nameMap.get( "priority" );
              if ( priorityObj instanceof String ) {
                priority = Integer.parseInt( (String) priorityObj );
              } else if ( priorityObj instanceof Number ) {
                priority = ( (Number) priorityObj ).intValue();
              }
            }
            int lastSlash = name.lastIndexOf( '/' );
            String path = "/";
            String defaultName = name;
            name = name + "*.properties";
            if ( lastSlash >= 0 ) {
              path = name.substring( 0, lastSlash );
              name = name.substring( lastSlash + 1 );
            }
            Enumeration<URL> urlEnumeration = bundle.findEntries( path, name, false );
            while ( urlEnumeration.hasMoreElements() ) {
              bundles.add( createResourceBundleFactory( defaultName, path, urlEnumeration.nextElement(), priority ) );
            }
          }
          configEntry.put( key, bundles );
          rebuildCache = true;
        }
      }
      synchronized ( configMap ) {
        configMap.put( bundle.getBundleId(), configEntry );
      }
    }
    if ( rebuildCache ) {
      synchronized ( configMap ) {
        if ( executorService == null ) {
          executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
            @Override
            public Thread newThread( Runnable r ) {
              Thread thread = Executors.defaultThreadFactory().newThread( r );
              thread.setDaemon( true );
              thread.setName( "Localization pool" );
              return thread;
            }
          } );
        }
        cache = executorService.submit( new OSGIResourceBundleCacheCallable(
          new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>( configMap ) ) );
      }
    }
  }

  private OSGIResourceBundleFactory createResourceBundleFactory( String defaultName, String path, URL url,
                                                                 int priority )
    throws IOException {
    String name = url.getPath();
    int lastSlash = name.lastIndexOf( '/' );
    if ( lastSlash >= 0 ) {
      name = name.substring( lastSlash );
    }

    return new OSGIResourceBundleFactory( defaultName, path + name, url, priority );
  }

  private JSONObject loadJsonObject( URL url ) throws IOException, ParseException {
    if ( url == null ) {
      return null;
    }
    URLConnection urlConnection = url.openConnection();
    InputStream inputStream = urlConnection.getInputStream();
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    StringBuilder sb = new StringBuilder();
    try {
      inputStreamReader = new InputStreamReader( urlConnection.getInputStream() );
      bufferedReader = new BufferedReader( inputStreamReader );
      return (JSONObject) parser.parse( bufferedReader );
    } finally {
      if ( bufferedReader != null ) {
        bufferedReader.close();
      }
      if ( inputStreamReader != null ) {
        inputStreamReader.close();
      }
      if ( inputStream != null ) {
        inputStream.close();
      }
    }
  }


  @Override public ResourceBundle getResourceBundle( String key, String name, Locale locale ) {
    if ( cache == null ) {
      return null;
    }
    try {
      Map<String, OSGIResourceBundle> factoryMap = cache.get().get( key );
      for ( String candidate : getCandidateNames( name, locale ) ) {
        OSGIResourceBundle bundle = factoryMap.get( candidate );
        if ( bundle != null ) {
          return bundle;
        }
      }
    } catch ( Exception e ) {
      log.error( e.getMessage(), e );
    }
    return null;
  }

  @Override public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Pattern nameRegex, Locale locale ) {
    if ( cache == null ) {
      return null;
    }
    List<ResourceBundle> result = new ArrayList<ResourceBundle>();
    try {
      for ( Map.Entry<String, Map<String, OSGIResourceBundle>> entry : cache.get().entrySet() ) {
        if ( keyRegex.matcher( entry.getKey() ).matches() ) {
          Map<String, OSGIResourceBundle> factoryMap = entry.getValue();
          Map<String, OSGIResourceBundle> matchingMap = factoryMap;
          Set<String> defaultNames = new HashSet<String>();
          if ( nameRegex != null ) {
            matchingMap = new HashMap<String, OSGIResourceBundle>();
            for ( Map.Entry<String, OSGIResourceBundle> factoryEntry : factoryMap.entrySet() ) {
              OSGIResourceBundle factoryEntryValue = factoryEntry.getValue();
              String defaultName = factoryEntryValue.getDefaultName();
              if ( nameRegex.matcher( defaultName ).matches() ) {
                defaultNames.add( defaultName );
                matchingMap.put( factoryEntry.getKey(), factoryEntryValue );
              }
            }
          } else {
            for ( OSGIResourceBundle osgiResourceBundle : factoryMap.values() ) {
              defaultNames.add( osgiResourceBundle.getDefaultName() );
            }
          }
          for ( String defaultName : defaultNames ) {
            for ( String candidate : getCandidateNames( defaultName, locale ) ) {
              OSGIResourceBundle bundle = matchingMap.get( candidate );
              if ( bundle != null ) {
                result.add( bundle );
                continue;
              }
            }
          }
        }
      }
    } catch ( Exception e ) {
      log.error( e.getMessage(), e );
    }
    return result;
  }

  @Override public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    return getResourceBundles( keyRegex, null, locale );
  }

  private List<String> getCandidateNames( String name, Locale locale ) {
    List<String> result = new ArrayList<String>();
    String current = name;
    result.add( current );
    String language = locale.getLanguage();
    if ( language != null && language.length() > 0 ) {
      current += "_" + language;
      result.add( current );
      String country = locale.getCountry();
      if ( country != null && country.length() > 0 ) {
        current += "_" + country;
        result.add( current );
      }
    }
    Collections.reverse( result );
    return result;
  }
}
