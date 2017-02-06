/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.js.require;

import org.json.simple.parser.JSONParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequireJsConfigManager {
  static final String PACKAGE_JSON_PATH = "META-INF/js/package.json";
  static final String REQUIRE_JSON_PATH = "META-INF/js/require.json";
  static final String EXTERNAL_RESOURCES_JSON_PATH = "META-INF/js/externalResources.json";
  static final String STATIC_RESOURCES_JSON_PATH = "META-INF/js/staticResources.json";

  private static final ScheduledExecutorService executorService =
      Executors.newScheduledThreadPool( 2, r -> {
        Thread thread = Executors.defaultThreadFactory().newThread( r );
        thread.setDaemon( true );
        thread.setName( "RequireJSConfigManager pool" );
        return thread;
      } );

  private final Map<Long, Map<String, Object>> configMap = new HashMap<>();
  private final Map<Long, RequireJsConfiguration> requireConfigMap = new HashMap<>();

  private final JSONParser parser = new JSONParser();
  private BundleContext bundleContext;

  private volatile ConcurrentHashMap<String, Future<String>> cachedConfigurations;
  private volatile long lastModified;

  private RequireJsBundleListener bundleListener;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void init() throws Exception {
    if ( this.bundleListener != null ) {
      throw new Exception( "Already initialized." );
    }

    // setting initial capacity to three (relative url and absolute http/https url scenarios)
    this.cachedConfigurations = new ConcurrentHashMap<>(3);

    this.bundleListener = new RequireJsBundleListener( this );
    this.bundleContext.addBundleListener( this.bundleListener );

    for ( Bundle bundle : this.bundleContext.getBundles() ) {
      this.updateBundleContext( bundle );
    }
    this.updateBundleContext( this.bundleContext.getBundle() );
  }

  public void destroy() {
    this.invalidateCachedConfigurations();

    if ( this.bundleListener != null ) {
      this.bundleContext.removeBundleListener( this.bundleListener );
    }

    this.bundleListener = null;
  }

  public void bundleChanged( Bundle bundle ) {
    boolean shouldRefresh = true;
    try {
      shouldRefresh = this.updateBundleContext( bundle );
    } catch ( Exception e ) {
      // Ignore TODO possibly log
    } finally {
      if ( shouldRefresh ) {
        this.invalidateCachedConfigurations();
      }
    }
  }

  public String getRequireJsConfig( String baseUrl ) {
    String result = null;
    int tries = 3;
    Exception lastException = null;
    while ( tries-- > 0 && result == null ) {
      Future<String> cache = this.getCachedConfiguration( baseUrl );

      try {
        result = cache.get();
      } catch ( InterruptedException e ) {
        // ignore
      } catch ( ExecutionException e ) {
        lastException = e;

        this.invalidateCachedConfigurations();
      }
    }

    if ( result == null ) {
      result = "{}; // Error computing RequireJS Config: ";
      if ( lastException != null && lastException.getCause() != null ) {
        result += lastException.getCause().getMessage();
      } else {
        result += "unknown error";
      }
    }

    return result;
  }

  public long getLastModified() {
    return this.lastModified;
  }

  private boolean updateBundleContext( Bundle bundle ) {
    switch ( bundle.getState() ) {
      case Bundle.STOPPING:
      case Bundle.RESOLVED:
      case Bundle.UNINSTALLED:
      case Bundle.INSTALLED:
        return this.updateBundleContextStopped( bundle );
      case Bundle.ACTIVE:
        return this.updateBundleContextActivated( bundle );
      default:
        return true;
    }
  }

  private boolean updateBundleContextStopped( Bundle bundle ) {
    Map<String, Object> bundleConfig = this.configMap.remove( bundle.getBundleId() );
    RequireJsConfiguration requireJsConfiguration = this.requireConfigMap.remove( bundle.getBundleId() );

    return bundleConfig != null || requireJsConfiguration != null;
  }

  private boolean updateBundleContextActivated( Bundle bundle ) {
    boolean shouldInvalidate = false;

    URL packageJsonUrl = bundle.getResource( PACKAGE_JSON_PATH );
    URL configFileUrl = bundle.getResource( REQUIRE_JSON_PATH );

    URL externalResourcesUrl = bundle.getResource( EXTERNAL_RESOURCES_JSON_PATH );
    URL staticResourcesUrl = bundle.getResource( STATIC_RESOURCES_JSON_PATH );

    if ( configFileUrl != null ) {
      Map<String, Object> requireJsonObject = this.loadJsonObject( configFileUrl );

      if ( requireJsonObject != null ) {
        this.putInConfigMap( bundle.getBundleId(), requireJsonObject );

        shouldInvalidate = true;
      }
    }

    if ( !shouldInvalidate && packageJsonUrl != null ) {
      shouldInvalidate = this.parsePackageInformation( bundle, packageJsonUrl );
    }

    if ( externalResourcesUrl != null ) {
      Map<String, Object> externalResourceJsonObject = this.loadJsonObject( externalResourcesUrl );
      Map<String, Object> staticResourceJsonObject = this.loadJsonObject( staticResourcesUrl );

      if ( externalResourceJsonObject != null ) {
        List<String> requireJsList = (List<String>) externalResourceJsonObject.get( "requirejs" );

        if ( requireJsList != null ) {
          if ( staticResourceJsonObject != null ) {
            List<String> translatedList = new ArrayList<>( requireJsList.size() );

            for ( String element : requireJsList ) {
              boolean found = false;
              for ( Object key : staticResourceJsonObject.keySet() ) {
                String strKey = key.toString();

                if ( element.startsWith( strKey ) ) {
                  String value = staticResourceJsonObject.get( key ).toString();
                  translatedList.add( value + element.substring( strKey.length() ) );
                  found = true;
                  break;
                }
              }

              if ( !found ) {
                translatedList.add( element );
              }
            }

            requireJsList = translatedList;
          }

          this.putInRequireConfigMap( bundle.getBundleId(), new RequireJsConfiguration( bundle, requireJsList ) );
          shouldInvalidate = true;
        }
      }
    }

    return shouldInvalidate;
  }

  private boolean parsePackageInformation( Bundle bundle, URL resourceUrl ) {
    try {
      URLConnection urlConnection = resourceUrl.openConnection();
      RequireJsGenerator gen = RequireJsGenerator.parseJsonPackage( urlConnection.getInputStream() );

      if ( gen != null ) {
        RequireJsGenerator.ArtifactInfo artifactInfo =
            new RequireJsGenerator.ArtifactInfo( "osgi-bundles", bundle.getSymbolicName(),
                bundle.getVersion().toString() );
        final RequireJsGenerator.ModuleInfo moduleInfo = gen.getConvertedConfig( artifactInfo );
        Map<String, Object> requireJsonObject = moduleInfo.getRequireJs();

        this.putInConfigMap( bundle.getBundleId(), requireJsonObject );

        return true;
      }
    } catch ( Exception ignored ) {
      // ignored
    }

    return false;
  }

  private void putInConfigMap( long bundleId, Map<String, Object> config ) {
    this.configMap.put( bundleId, config );
  }

  private void putInRequireConfigMap( long bundleId, RequireJsConfiguration config ) {
    this.requireConfigMap.put( bundleId, config );
  }

  private Map<String, Object> loadJsonObject( URL url ) {
    if ( url == null ) {
      return null;
    }

    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;

    try {
      URLConnection urlConnection = url.openConnection();
      inputStream = urlConnection.getInputStream();
      inputStreamReader = new InputStreamReader( urlConnection.getInputStream() );
      bufferedReader = new BufferedReader( inputStreamReader );

      return (Map<String, Object>) this.parser.parse( bufferedReader );
    } catch ( Exception ignored ) {
      // ignored
    } finally {
      try {
        if ( bufferedReader != null ) {
          bufferedReader.close();
        }
        if ( inputStreamReader != null ) {
          inputStreamReader.close();
        }
        if ( inputStream != null ) {
          inputStream.close();
        }
      } catch ( IOException ignored ) {
        // ignored
      }
    }

    return null;
  }

  // package-private for unit testing
  Future<String> getCachedConfiguration( String baseUrl ) {
    return this.cachedConfigurations.computeIfAbsent( baseUrl, k -> executorService.schedule( new RebuildCacheCallable( baseUrl, new HashMap<>( this.configMap ),
        new ArrayList<>( this.requireConfigMap.values() ) ), 250, TimeUnit.MILLISECONDS ) );
  }

  // package-private for unit testing
  void invalidateCachedConfigurations() {
    this.lastModified = System.currentTimeMillis();

    this.cachedConfigurations.forEach( ( s, stringFuture ) -> stringFuture.cancel( true ) );
    this.cachedConfigurations.clear();
  }

}
