/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.requirejs.IPlatformPluginRequireJsConfigurations;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.impl.RequireJsConfigManager;
import org.pentaho.requirejs.impl.types.MetaInfPackageJson;
import org.pentaho.requirejs.impl.types.MetaInfRequireJson;
import org.pentaho.requirejs.impl.types.BundledPlatformPluginRequireJsConfigurations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to and processes bundles that include some sort of RequireJS configuration information.
 *
 * For bundles with META-INF/js/package.json or META-INF/js/require.json files, parses the json file
 * and registers a corresponding {@link IRequireJsPackage} service implementation, to be dealt by {@link RequireJsPackageServiceTracker}.
 * The service reference is maintained so it can be unregistered when the bundle stops.
 *
 * For bundles with META-INF/js/externalResources.json file (and optionally an accompanying META-INF/js/staticResources.json file),
 * a {@link IPlatformPluginRequireJsConfigurations} instance is created, to be returned by {@link #getScripts()} until the
 * corresponding bundle is stopped.
 */
public class RequireJsBundleListener implements BundleListener {
  static final String PACKAGE_JSON_PATH = "META-INF/js/package.json";
  static final String REQUIRE_JSON_PATH = "META-INF/js/require.json";

  public static final String EXTERNAL_RESOURCES_JSON_PATH = "META-INF/js/externalResources.json";
  public static final String STATIC_RESOURCES_JSON_PATH = "META-INF/js/staticResources.json";

  private BundleContext bundleContext;

  private RequireJsConfigManager requireJsConfigManager;

  private Map<Long, ServiceRegistration<?>> serviceRegistrationMap;
  private Map<Long, IPlatformPluginRequireJsConfigurations> requireConfigMap;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void setRequireJsConfigManager( RequireJsConfigManager requireJsConfigManager ) {
    this.requireJsConfigManager = requireJsConfigManager;
  }

  public void init() {
    this.serviceRegistrationMap = new ConcurrentHashMap<>();
    this.requireConfigMap = new ConcurrentHashMap<>();

    this.bundleContext.addBundleListener( this );

    for ( Bundle bundle : this.bundleContext.getBundles() ) {
      this.addBundle( bundle );
    }
  }

  public void destroy() {
    this.serviceRegistrationMap = null;
    this.requireConfigMap = null;

    this.bundleContext.removeBundleListener( this );
  }

  public Collection<IPlatformPluginRequireJsConfigurations> getScripts() {
    return Collections.unmodifiableCollection( this.requireConfigMap.values() );
  }

  @Override
  public void bundleChanged( BundleEvent bundleEvent ) {
    if ( this.isListenerActive() ) {
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

      if ( shouldInvalidate && this.isListenerActive() ) {
        this.requireJsConfigManager.invalidateCachedConfigurations();
      }
    }
  }

  /**
   * @param bundle
   * @return true only if any bundles with META-INF/js/externalResources.json file was removed.
   */
  public boolean removeBundle( Bundle bundle ) {
    return removeBundleInternal( bundle );
  }

  private boolean removeBundleInternal( Bundle bundle ) {
    ServiceRegistration<?> serviceRegistration = this.serviceRegistrationMap.remove( bundle.getBundleId() );
    if ( serviceRegistration != null ) {
      try {
        serviceRegistration.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }
    }

    IPlatformPluginRequireJsConfigurations requireJsConfiguration = this.requireConfigMap.remove( bundle.getBundleId() );

    return requireJsConfiguration != null;
  }

  /**
   * @param bundle
   * @return true only if any bundles with META-INF/js/externalResources.json file was added / updated.
   */
  boolean addBundle( Bundle bundle ) {
    boolean shouldInvalidate;

    try {
      if ( bundle.getState() != Bundle.ACTIVE ) {
        return false;
      }

      // clear any previous configurations (for bundle updates)
      shouldInvalidate = removeBundleInternal( bundle );

      URL packageJsonUrl = bundle.getResource( PACKAGE_JSON_PATH );
      URL configFileUrl = bundle.getResource( REQUIRE_JSON_PATH );

      URL externalResourcesUrl = bundle.getResource( EXTERNAL_RESOURCES_JSON_PATH );
      URL staticResourcesUrl = bundle.getResource( STATIC_RESOURCES_JSON_PATH );

      if ( configFileUrl != null ) {
        // top priority: legacy META-INF/js/require.json
        Map<String, Object> requireJsonObject = this.loadJsonObject( configFileUrl );

        if ( requireJsonObject != null ) {
          IRequireJsPackage packageInfo = new MetaInfRequireJson( requireJsonObject );

          ServiceRegistration<?> serviceRegistration = bundle.getBundleContext().registerService( IRequireJsPackage.class.getName(), packageInfo, null );
          this.serviceRegistrationMap.put( bundle.getBundleId(), serviceRegistration );
        }
      } else if ( packageJsonUrl != null ) {
        // next: fixed META-INF/js/package.json
        Map<String, Object> packageJsonObject = this.loadJsonObject( packageJsonUrl );

        if ( packageJsonObject != null ) {
          IRequireJsPackage packageInfo = new MetaInfPackageJson( packageJsonObject );

          ServiceRegistration<?> serviceRegistration = bundle.getBundleContext().registerService( IRequireJsPackage.class.getName(), packageInfo, null );
          this.serviceRegistrationMap.put( bundle.getBundleId(), serviceRegistration );
        }
      }

      // always process legacy META-INF/js/externalResources.json and META-INF/js/staticResources.json
      if ( externalResourcesUrl != null ) {
        Map<String, Object> externalResourceJsonObject = this.loadJsonObject( externalResourcesUrl );
        Map<String, Object> staticResourceJsonObject = this.loadJsonObject( staticResourcesUrl );

        if ( externalResourceJsonObject != null ) {
          List<String> requireJsList = getRequireJsList( externalResourceJsonObject, staticResourceJsonObject );

          if ( requireJsList != null ) {
            this.requireConfigMap.put( bundle.getBundleId(), new BundledPlatformPluginRequireJsConfigurations( bundle, requireJsList ) );
            shouldInvalidate = true;
          }
        }
      }
    } catch ( Exception e ) {
      shouldInvalidate = true;
    }

    return shouldInvalidate;
  }

  private List<String> getRequireJsList( Map<String, Object> externalResourceJsonObject, Map<String, Object> staticResourceJsonObject ) {
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
    }

    return requireJsList;
  }

  private boolean isListenerActive() {
    return this.requireConfigMap != null && this.serviceRegistrationMap != null;
  }

  private Map<String, Object> loadJsonObject( URL url ) throws IOException, ParseException {
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

      return (Map<String, Object>) (new JSONParser()).parse( bufferedReader );
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
}
