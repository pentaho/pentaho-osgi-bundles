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
 * Copyright 2016 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
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
import java.util.regex.Pattern;

public class LocalizationManager implements LocalizationService {
  private static Logger log = LoggerFactory.getLogger( LocalizationManager.class );

  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap = new HashMap<>();
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
    synchronized ( this.configMap ) {
      rebuildCache = this.configMap.remove( bundle.getBundleId() ) != null;
    }

    if ( bundle.getState() == Bundle.ACTIVE ) {
      List<String> webPackageRoots = getBundleRoots( bundle );
      for ( String root : webPackageRoots ) {
        if ( addBundleResources( bundle, root ) ) {
          rebuildCache = true;
        }
      }
    }

    if ( rebuildCache ) {
      synchronized ( this.configMap ) {
        if ( this.executorService == null ) {
          this.executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
            @Override
            public Thread newThread( Runnable r ) {
              Thread thread = Executors.defaultThreadFactory().newThread( r );
              thread.setDaemon( true );
              thread.setName( "Localization pool" );
              return thread;
            }
          } );
        }

        this.cache = this.executorService
            .submit( new OSGIResourceBundleCacheCallable( new HashMap<>( this.configMap ) ) );
      }
    }
  }

  @Override
  public ResourceBundle getResourceBundle( Class clazz, String key, Locale locale ) {
    String absoluteKey = "/i18n/" + key.replaceAll( "\\.", "/" );

    return this.getResourceBundle( absoluteKey, locale );
  }

  @Override
  public ResourceBundle getResourceBundle( Bundle bundle, String key, Locale locale ) {
    return this.getResourceBundle( key, locale );
  }

  @Override
  public ResourceBundle getResourceBundle( String key, Locale locale ) {
    ResourceBundle result = null;

    // Temporary, so that server side code doesn't break
    String absoluteKey = key.startsWith( "/" ) ? key : "/i18n/" + key.replaceAll( "\\.", "/" );
    Map<String, OSGIResourceBundle> localCache = getCache();

    if ( localCache != null ) {
      if ( absoluteKey != null ) {
        for ( String candidate : OSGIResourceNamingConvention.getCandidateNames( absoluteKey, locale ) ) {
          OSGIResourceBundle osgiResourceBundle = localCache.get( candidate );
          if ( osgiResourceBundle != null ) {
            result = osgiResourceBundle;
            break;
          }
        }
      }
    }

    return result;
  }

  @Override
  public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    List<ResourceBundle> result = new ArrayList<>();
    Map<String, OSGIResourceBundle> localCache = getCache();

    if ( localCache != null ) {
      Set<String> defaultNames = new HashSet<>();
      Map<String, OSGIResourceBundle> matchingMap = new HashMap<>();
      for ( Map.Entry<String, OSGIResourceBundle> factoryEntry : localCache.entrySet() ) {
        OSGIResourceBundle factoryEntryValue = factoryEntry.getValue();
        String defaultName = factoryEntryValue.getDefaultName();
        if ( keyRegex.matcher( defaultName ).matches() ) {
          defaultNames.add( defaultName );
          matchingMap.put( factoryEntry.getKey(), factoryEntryValue );
        }
      }

      for ( String defaultName : defaultNames ) {
        for ( String candidate : OSGIResourceNamingConvention.getCandidateNames( defaultName, locale ) ) {
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

  private Map<String, OSGIResourceBundle> getCache() {
    Map<String, OSGIResourceBundle> result = null;
    if ( this.cache != null ) {
      try {
        result = this.cache.get();
      } catch ( Exception e ) {
        getLog().error( e.getMessage(), e );
      }
    }

    return result;
  }

  /**
   * Returns property file name without extension
   *
   * @param fileName
   *
   * @return property file name without extension
   */
  private String getPropertyName( String filename ) {
    return filename.replaceAll( "\\.properties.*$", "" );
  }

  private List<String> getBundleRoots( Bundle bundle ) {
    List<String> webPackageRoots = new ArrayList<>();
    webPackageRoots.add( OSGIResourceNamingConvention.RESOURCES_I18N_FOLDER );

    BundleWiring wiring = bundle.adapt( BundleWiring.class );
    if ( wiring != null ) {
      List<BundleCapability> capabilities = wiring.getCapabilities( CAPABILITY_NAMESPACE );
      for ( BundleCapability bundleCapability : capabilities ) {
        Map<String, Object> attributes = bundleCapability.getAttributes();

        webPackageRoots.add( (String) attributes.getOrDefault( "root", "" ) );
      }
    }

    return webPackageRoots;
  }

  private boolean addBundleResources( Bundle bundle, String root ) {

    Map<String, OSGIResourceBundleFactory> configEntry = new HashMap<>();
    OSGIResourceBundleFactory bundleFactory;

    final String i18nResourcePattern = "*" + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION + "*";
    Enumeration<URL> urlEnumeration = bundle.findEntries( root, i18nResourcePattern, true );
    if ( urlEnumeration != null ) {
      while ( urlEnumeration.hasMoreElements() ) {
        URL url = urlEnumeration.nextElement();
        if ( url != null ) {
          String filename = url.getFile();
          String resourceKey = getPropertyName( filename );

          int priority = OSGIResourceNamingConvention.getPropertyPriority( filename );
          bundleFactory = new OSGIResourceBundleFactory( resourceKey, filename, url, priority );
          configEntry.put( resourceKey, bundleFactory );
        }
      }
    }

    boolean isConfigEmpty = configEntry.isEmpty();
    if ( !isConfigEmpty ) {
      synchronized ( this.configMap ) {
        this.configMap.put( bundle.getBundleId(), configEntry );
      }
    }

    return !isConfigEmpty;
  }

}
