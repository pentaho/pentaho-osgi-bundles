/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.requirejs.impl.listeners;

import org.json.simple.parser.JSONParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.pentaho.requirejs.RequireJsPackage;
import org.pentaho.requirejs.impl.RequireJsConfigManager;
import org.pentaho.requirejs.impl.types.MetaInfPackageJson;
import org.pentaho.requirejs.impl.types.MetaInfRequireJson;
import org.pentaho.requirejs.impl.types.RequireJsConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to and processes bundles that include some sort of RequireJS configuration information.
 *
 * For bundles with META-INF/js/package.json or META-INF/js/require.json files, parses the json file
 * and registers a corresponding {@link RequireJsPackage} service implementation, to be dealt by {@link RequireJsPackageServiceTracker}.
 * The service reference is maintained so it can be unregistered when the bundle stops.
 *
 * For bundles with META-INF/js/externalResources.json file (and optionally an accompanying META-INF/js/staticResources.json file),
 * a {@link RequireJsConfiguration} instance is created, to be returned by {@link #getExternalResourcesRequireJsScripts()} until the
 * corresponding bundle is stopped.
 */
public class RequireJsBundleListener implements BundleListener {
  static final String PACKAGE_JSON_PATH = "META-INF/js/package.json";
  static final String REQUIRE_JSON_PATH = "META-INF/js/require.json";
  static final String EXTERNAL_RESOURCES_JSON_PATH = "META-INF/js/externalResources.json";
  static final String STATIC_RESOURCES_JSON_PATH = "META-INF/js/staticResources.json";

  private final RequireJsConfigManager requireJsConfigManager;

  private final Map<Long, RequireJsPackage> configMap;
  private final Map<Long, RequireJsConfiguration> requireConfigMap;

  private final JSONParser parser = new JSONParser();

  public RequireJsBundleListener( RequireJsConfigManager requireJsConfigManager ) {
    this.requireJsConfigManager = requireJsConfigManager;

    this.configMap = new ConcurrentHashMap<>();
    this.requireConfigMap = new ConcurrentHashMap<>();
  }

  public Collection<RequireJsConfiguration> getExternalResourcesRequireJsScripts() {
    return Collections.unmodifiableCollection( this.requireConfigMap.values() );
  }

  @Override
  public void bundleChanged( BundleEvent bundleEvent ) {
    final Bundle bundle = bundleEvent.getBundle();

    final int bundleEventType = bundleEvent.getType();

    boolean shouldInvalidate = false;

    if ( bundleEventType == BundleEvent.STARTED ) {
      shouldInvalidate = addBundle( bundle );
    } else if ( bundleEventType == BundleEvent.UNINSTALLED
        || bundleEventType == BundleEvent.UNRESOLVED
        || bundleEventType == BundleEvent.STOPPED ) {
      shouldInvalidate = removeBundle( bundle );
    }

    if ( shouldInvalidate ) {
      this.requireJsConfigManager.invalidateCachedConfigurations();
    }
  }

  private boolean removeBundle( Bundle bundle ) {
    RequireJsPackage bundleConfig = this.configMap.remove( bundle.getBundleId() );
    if ( bundleConfig != null ) {
      bundleConfig.unregister();
    }

    RequireJsConfiguration requireJsConfiguration = this.requireConfigMap.remove( bundle.getBundleId() );

    return bundleConfig != null || requireJsConfiguration != null;
  }

  public boolean addBundle( Bundle bundle ) {
    // clear any previous configurations (for bundle updates)
    boolean shouldInvalidate = removeBundle( bundle );

    URL packageJsonUrl = bundle.getResource( PACKAGE_JSON_PATH );
    URL configFileUrl = bundle.getResource( REQUIRE_JSON_PATH );

    URL externalResourcesUrl = bundle.getResource( EXTERNAL_RESOURCES_JSON_PATH );
    URL staticResourcesUrl = bundle.getResource( STATIC_RESOURCES_JSON_PATH );

    if ( configFileUrl != null ) {
      // top priority: legacy META-INF/js/require.json
      Map<String, Object> requireJsonObject = this.loadJsonObject( configFileUrl );

      if ( requireJsonObject != null ) {
        RequireJsPackage packageInfo = new MetaInfRequireJson( bundle.getBundleContext(), requireJsonObject );
        packageInfo.register();

        this.configMap.put( bundle.getBundleId(), packageInfo );

        shouldInvalidate = true;
      }
    } else if ( packageJsonUrl != null ) {
      // next: fixed META-INF/js/package.json
      Map<String, Object> packageJsonObject = this.loadJsonObject( packageJsonUrl );

      if ( packageJsonObject != null ) {
        RequireJsPackage packageInfo = new MetaInfPackageJson( bundle.getBundleContext(), packageJsonObject );
        packageInfo.register();

        this.configMap.put( bundle.getBundleId(), packageInfo );

        shouldInvalidate = true;
      }
    }

    // always process legacy META-INF/js/externalResources.json and META-INF/js/staticResources.json
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

          this.requireConfigMap.put( bundle.getBundleId(), new RequireJsConfiguration( bundle, requireJsList ) );
          shouldInvalidate = true;
        }
      }
    }

    return shouldInvalidate;
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
}
