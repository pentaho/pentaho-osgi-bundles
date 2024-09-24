/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl;

import org.json.simple.JSONObject;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;
import org.pentaho.requirejs.impl.listeners.RequireJsBundleListener;
import org.pentaho.requirejs.impl.listeners.RequireJsPackageServiceTracker;
import org.pentaho.requirejs.impl.servlet.RebuildCacheCallable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequireJsConfigManager {
  private static final ScheduledExecutorService executorService =
      Executors.newScheduledThreadPool( 2, r -> {
        Thread thread = Executors.defaultThreadFactory().newThread( r );
        thread.setDaemon( true );
        thread.setName( "RequireJSConfigManager pool" );
        return thread;
      } );

  private RequireJsPackageServiceTracker packageConfigurationsTracker;
  private RequireJsBundleListener externalResourcesScriptsTracker;

  /**
   * Plugins that can customize each package's requirejs configuration.
   */
  private List<IRequireJsPackageConfigurationPlugin> plugins;

  // setting initial capacity to three (relative url and absolute http/https url scenarios)
  private volatile ConcurrentHashMap<String, Future<String>> cachedConfigurations = new ConcurrentHashMap<>( 3 );
  private volatile ConcurrentHashMap<String, String> cachedContextMapping = new ConcurrentHashMap<>();

  public void setPackageConfigurationsTracker( RequireJsPackageServiceTracker packageConfigurationsTracker ) {
    this.packageConfigurationsTracker = packageConfigurationsTracker;
  }

  public void setExternalResourcesScriptsTracker( RequireJsBundleListener externalResourcesScriptsTracker ) {
    this.externalResourcesScriptsTracker = externalResourcesScriptsTracker;
  }

  public void setPlugins( List<IRequireJsPackageConfigurationPlugin> plugins ) {
    this.plugins = plugins;
  }

  public void destroy() {
    this.invalidateCachedConfigurations();
  }

  public String getRequireJsConfig( String baseUrl ) {
    // Make sure the baseUrl ends in a slash.
    // like https://github.com/requirejs/requirejs/blob/14526943c937aab3c022235335f20e260395fe15/require.js#L1145
    baseUrl = baseUrl.endsWith( "/" ) ? baseUrl : baseUrl + "/";

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

  public String getContextMapping( String baseUrl, String referer ) {
    return this.getCachedContextMapping( baseUrl, referer );
  }

  public void invalidateCachedConfigurations() {
    this.cachedConfigurations.forEach( ( s, stringFuture ) -> stringFuture.cancel( true ) );
    this.cachedConfigurations.clear();

    this.cachedContextMapping.clear();
  }

  Future<String> getCachedConfiguration( String baseUrl ) {
    return this.cachedConfigurations.computeIfAbsent( baseUrl, key -> executorService.schedule( createRebuildCacheCallable( key ), 250, TimeUnit.MILLISECONDS ) );
  }

  private String getCachedContextMapping( String baseUrl, String referer ) {
    if ( referer != null ) {
      return this.cachedContextMapping.computeIfAbsent( referer, k -> {
        List<IRequireJsPackageConfiguration> requireJsPackageConfigurations = this.packageConfigurationsTracker.getPackages();

        for ( IRequireJsPackageConfiguration requireJsPackage : requireJsPackageConfigurations ) {
          String webRootPath = requireJsPackage.getWebRootPath();
          // Compare values in lowercase to ensure that contains works well, because some http clients force requests to lowercase
          if ( webRootPath != null && !webRootPath.isEmpty() && referer.toLowerCase().contains( (baseUrl + webRootPath).toLowerCase() ) ) {
            Map<String, Object> contextConfig = new HashMap<>();
            Map<String, Map<String, String>> topMap = new HashMap<>();
            Map<String, String> map = new HashMap<>();

            requireJsPackage.getModuleIdsMapping().forEach( map::put );

            topMap.put( "*", map );
            contextConfig.put( "map", topMap );

            return JSONObject.toJSONString( contextConfig );
          }
        }

        return null;
      } );
    }

    return null;
  }

  // region package-private factory methods for unit testing
  Callable<String> createRebuildCacheCallable( String baseUrl ) {
    return new RebuildCacheCallable( baseUrl, this.packageConfigurationsTracker.getPackages(), this.externalResourcesScriptsTracker.getScripts(), this.plugins );
  }
  // endregion
}
