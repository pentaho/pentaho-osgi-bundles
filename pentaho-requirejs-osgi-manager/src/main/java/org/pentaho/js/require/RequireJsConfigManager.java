/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Created by bryan on 8/5/14.
 */
public class RequireJsConfigManager {
  public static final String PACKAGE_JSON_PATH = "META-INF/js/package.json";
  public static final String REQUIRE_JSON_PATH = "META-INF/js/require.json";
  public static final String EXTERNAL_RESOURCES_JSON_PATH = "META-INF/js/externalResources.json";
  public static final String STATIC_RESOURCES_JSON_PATH = "META-INF/js/staticResources.json";

  private final Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
  private final Map<Long, RequireJsConfiguration> requireConfigMap = new HashMap<Long, RequireJsConfiguration>();

  private final JSONParser parser = new JSONParser();
  private BundleContext bundleContext;

  private static ExecutorService executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
    @Override
    public Thread newThread( Runnable r ) {
      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "RequireJSConfigManager pool" );
      return thread;
    }
  } );

  private volatile Future<String> cache;
  private volatile long lastModified;

  private String contextRoot = "/";
  private RequireJsBundleListener bundleListener;

  public BundleContext getBundleContext() {
    return bundleContext;
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public boolean updateBundleContext( Bundle bundle ) throws IOException, ParseException {
    boolean shouldInvalidate = updateBundleContextStopped( bundle );

    URL packageJsonUrl = bundle.getResource( PACKAGE_JSON_PATH );
    URL configFileUrl = bundle.getResource( REQUIRE_JSON_PATH );

    URL externalResourcesUrl = bundle.getResource( EXTERNAL_RESOURCES_JSON_PATH );
    URL staticResourcesUrl = bundle.getResource( STATIC_RESOURCES_JSON_PATH );

    if ( packageJsonUrl != null ) {
      JSONObject packageJsonObject = loadJsonObject( packageJsonUrl );

      if ( packageJsonObject != null ) {
        RequireJsGenerator gen = new RequireJsGenerator( packageJsonObject );
        RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator.ArtifactInfo( "osgi-bundles", bundle.getSymbolicName(), bundle.getVersion().toString() );
        final RequireJsGenerator.ModuleInfo moduleInfo = gen.getConvertedConfig( artifactInfo );
        JSONObject requireJsonObject = moduleInfo.getRequirejs();

        putInConfigMap( bundle.getBundleId(), requireJsonObject );

        shouldInvalidate = true;
      }
    } else if ( configFileUrl != null ) {
      JSONObject requireJsonObject = loadJsonObject( configFileUrl );

      if ( requireJsonObject != null ) {
        putInConfigMap( bundle.getBundleId(), requireJsonObject );

        shouldInvalidate = true;
      }
    }

    if ( externalResourcesUrl != null ) {
      JSONObject externalResourceJsonObject = loadJsonObject( externalResourcesUrl );
      JSONObject staticResourceJsonObject = loadJsonObject( staticResourcesUrl );

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

          putInRequireConfigMap( bundle.getBundleId(), new RequireJsConfiguration( bundle, requireJsList ) );
          shouldInvalidate = true;
        }
      }
    }

    return shouldInvalidate;
  }

  private synchronized void putInConfigMap( long bundleId, JSONObject config ) {
    configMap.put( bundleId, config );
  }

  private synchronized void putInRequireConfigMap( long bundleId, RequireJsConfiguration config ) {
    requireConfigMap.put( bundleId, config );
  }

  private JSONObject loadJsonObject( URL url ) throws IOException, ParseException {
    if ( url == null ) {
      return null;
    }
    URLConnection urlConnection = url.openConnection();
    InputStream inputStream = urlConnection.getInputStream();
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
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

  public boolean updateBundleContextStopped( Bundle bundle ) {
    JSONObject bundleConfig;
    RequireJsConfiguration requireJsConfiguration;
    synchronized ( configMap ) {
      bundleConfig = configMap.remove( bundle.getBundleId() );
      requireJsConfiguration = requireConfigMap.remove( bundle.getBundleId() );
    }
    return bundleConfig != null || requireJsConfiguration != null;
  }

  public void invalidateCache( boolean shouldInvalidate ) {
    if ( shouldInvalidate ) {
      synchronized ( configMap ) {
        synchronized ( requireConfigMap ) {
          cache = executorService.submit( new RebuildCacheCallable( new HashMap<>( this.configMap ),
            new ArrayList<>( requireConfigMap.values() ) ) );
          lastModified = System.currentTimeMillis();
        }
      }
    }
  }

  public String getRequireJsConfig() {
    Future<String> cache = null;
    String result = null;
    int tries = 3;
    Exception lastException = null;
    while ( tries-- > 0 && ( result == null || cache != this.cache ) ) {
      cache = this.cache;
      try {
        result = cache.get();
      } catch ( InterruptedException e ) {
        // ignore
      } catch ( ExecutionException e ) {
        lastException = e;
        invalidateCache( true );
      }
    }
    if ( result == null ) {
      result = "{}; // Error computing RequireJS Config: ";
      if ( lastException != null ) {
        result += lastException.getCause().getMessage();
      } else {
        result += "unknown error";
      }
    }
    return result;
  }

  public long getLastModified() {
    return lastModified;
  }

  protected void setLastModified( long lastModified ) {
    this.lastModified = lastModified;
  }

  public void bundleChanged( Bundle bundle ) {
    boolean shouldRefresh = true;
    try {
      shouldRefresh = updateBundleContext( bundle );
    } catch ( Exception e ) {
      // Ignore TODO possibly log
    } finally {
      invalidateCache( shouldRefresh );
    }
  }

  public void init() throws Exception {
    bundleListener = new RequireJsBundleListener( this );
    bundleContext.addBundleListener( bundleListener );
    for ( Bundle bundle : bundleContext.getBundles() ) {
      updateBundleContext( bundle );
    }
    updateBundleContext( bundleContext.getBundle() );
    invalidateCache( true );
  }

  public void destroy() {
    if ( bundleListener != null ) {
      bundleContext.removeBundleListener( bundleListener );
    }
  }

  public String getContextRoot() {
    return this.contextRoot;
  }

  public void setContextRoot( String contextRoot ) {
    // ensure that the given string is properly bounded with slashes
    contextRoot = ( contextRoot.startsWith( "/" ) == false ) ? "/" + contextRoot : contextRoot;
    contextRoot = ( contextRoot.endsWith( "/" ) == false ) ? contextRoot + "/" : contextRoot;
    this.contextRoot = contextRoot;
  }
}
