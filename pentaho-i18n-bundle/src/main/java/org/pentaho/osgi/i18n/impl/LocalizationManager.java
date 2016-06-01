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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundle;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleCacheCallable;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleFactory;
import org.pentaho.osgi.i18n.settings.OSGIResourceNamingConvention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/4/14.
 */
public class LocalizationManager implements LocalizationService {
  private static Logger log = LoggerFactory.getLogger( LocalizationManager.class );
  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
    new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
  private final JSONParser parser = new JSONParser();
  private ExecutorService executorService;
  private volatile Future<Map<String, OSGIResourceBundle>> cache;

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

    Map<String, OSGIResourceBundleFactory> configEntry = new HashMap<String, OSGIResourceBundleFactory>();
    OSGIResourceBundleFactory bundleFactory;
    Enumeration<URL> urlEnumeration =
      bundle.findEntries( OSGIResourceNamingConvention.RESOURCES_ROOT_FOLDER,
        "*" + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION + "*", true );
    if ( urlEnumeration != null ) {
      while ( urlEnumeration.hasMoreElements() ) {
        URL url = urlEnumeration.nextElement();
        if ( url != null ) {
          String fileName = url.getFile();
          String relativeName = getPropertyRelativeName( fileName );
          String name = getPropertyName( fileName );
          int priority = getPropertyPriority( fileName );
          bundleFactory = new OSGIResourceBundleFactory( name, relativeName, url, priority );
          configEntry.put( name, bundleFactory );
          rebuildCache = true;
        }
      }
    }

    if ( !configEntry.isEmpty() ) {
      synchronized ( configMap ) {
        configMap.put( bundle.getBundleId(), configEntry );
      }
      rebuildCache = true;
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
          new HashMap<Long, Map<String, OSGIResourceBundleFactory>>( configMap ) ) );
      }
    }
  }

  /**
   * Returns property's relative name without priority
   *
   * @param fileName
   * @return
   */
  private String getPropertyRelativeName( String fileName ) {
    if ( fileName.indexOf( "/" ) == 0 ) {
      fileName = fileName.substring( 1 );
    }
    Matcher matcher = OSGIResourceNamingConvention.getResourceNameMatcher( fileName );
    String groop = matcher.group( matcher.groupCount() );
    if ( groop != null ) {
      fileName = fileName.replace( groop, "" );
    }
    return fileName;
  }

  /**
   * Returns property file name without extension
   *
   * @param fileName
   * @return property file name without extension
   */
  private String getPropertyName( String fileName ) {
    int index = fileName.lastIndexOf( OSGIResourceNamingConvention.RESOURCES_ROOT_FOLDER )
      + OSGIResourceNamingConvention.RESOURCES_ROOT_FOLDER.length();
    return fileName.substring( index + 1,
      fileName.lastIndexOf( OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION ) );
  }

  /**
   * Returns property priority propertyName must have message-name.properties.5 format (priority = 5)
   *
   * @param propertyName
   * @return propertyPriority, default priority = 0
   */
  private int getPropertyPriority( String propertyName ) {
    int priority = 0;
    Matcher matcher = OSGIResourceNamingConvention.getResourceNameMatcher( propertyName );
    String groop = matcher.group( matcher.groupCount() );
    if ( groop != null ) {
      priority = Integer.parseInt( groop.substring( 1 ) );
    }
    return priority;
  }

  @Override
  public ResourceBundle getResourceBundle( String name, Locale locale ) {
    ResourceBundle result = null;
    Map<String, OSGIResourceBundle> localCache = getCache();

    if ( localCache != null ) {
      for ( String candidate : getCandidateNames( name.replaceAll( "\\.", "/" ), locale ) ) {
        OSGIResourceBundle bundle = localCache.get( candidate );
        if ( bundle != null ) {
          result = bundle;
          break;
        }
      }
    }
    return result;
  }

  @Override
  public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    List<ResourceBundle> result = new ArrayList<ResourceBundle>();
    Map<String, OSGIResourceBundle> localCache = getCache();

    if ( localCache != null ) {
      Set<String> defaultNames = new HashSet<String>();
      Map<String, OSGIResourceBundle> matchingMap = new HashMap<String, OSGIResourceBundle>();
      for ( Map.Entry<String, OSGIResourceBundle> factoryEntry : localCache.entrySet() ) {
        OSGIResourceBundle factoryEntryValue = factoryEntry.getValue();
        String defaultName = factoryEntryValue.getDefaultName();
        if ( keyRegex.matcher( defaultName ).matches() ) {
          defaultNames.add( defaultName );
          matchingMap.put( factoryEntry.getKey(), factoryEntryValue );
        }
      }
      for ( String defaultName : defaultNames ) {
        for ( String candidate : getCandidateNames( defaultName, locale ) ) {
          OSGIResourceBundle bundle = localCache.get( candidate );
          if ( bundle != null ) {
            result.add( bundle );
            continue;
          }
        }
      }
    } else {
      result = null;
    }
    return result;
  }

  private List<String> getCandidateNames( String name, Locale locale ) {
    List<String> result = new ArrayList<String>();
    String current = name;
    result.add( current );
    String language = locale.getLanguage();
    if ( language.length() > 0 ) {
      current += "_" + language;
      result.add( current );
      String country = locale.getCountry();
      if ( country.length() > 0 ) {
        current += "_" + country;
        result.add( current );
      }
    }
    Collections.reverse( result );
    return result;
  }

  private Map<String, OSGIResourceBundle> getCache() {
    Map<String, OSGIResourceBundle> result = null;
    if ( cache != null ) {
      try {
        result = cache.get();
      } catch ( Exception e ) {
        log.error( e.getMessage(), e );
      }
    }
    return result;
  }
}
