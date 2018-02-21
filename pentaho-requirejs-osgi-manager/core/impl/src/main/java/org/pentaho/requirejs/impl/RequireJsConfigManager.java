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
package org.pentaho.requirejs.impl;

import org.json.simple.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.requirejs.RequireJsPackage;
import org.pentaho.requirejs.RequireJsPackageConfiguration;
import org.pentaho.requirejs.impl.listeners.RequireJsBundleListener;
import org.pentaho.requirejs.impl.listeners.RequireJsPackageServiceTracker;
import org.pentaho.requirejs.impl.servlet.RebuildCacheCallable;
import org.pentaho.requirejs.impl.servlet.RequireJsConfigServlet;

import java.util.ArrayList;
import java.util.Collection;
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
  private static final ScheduledExecutorService executorService =
      Executors.newScheduledThreadPool( 2, r -> {
        Thread thread = Executors.defaultThreadFactory().newThread( r );
        thread.setDaemon( true );
        thread.setName( "RequireJSConfigManager pool" );
        return thread;
      } );

  private BundleContext bundleContext;

  private volatile ConcurrentHashMap<String, Future<String>> cachedConfigurations;
  private volatile ConcurrentHashMap<String, String> cachedContextMapping;

  private ServiceTracker<RequireJsPackage, RequireJsPackageConfiguration> requireJsPackageServiceTracker;
  private RequireJsBundleListener bundleListener;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void init() throws Exception {
    if ( this.bundleListener != null || this.requireJsPackageServiceTracker != null ) {
      throw new Exception( "Already initialized." );
    }

    this.requireJsPackageServiceTracker = createRequireJsPackageServiceTracker( this.bundleContext );
    this.requireJsPackageServiceTracker.open( true );

    // setting initial capacity to three (relative url and absolute http/https url scenarios)
    this.cachedConfigurations = new ConcurrentHashMap<>( 3 );

    this.cachedContextMapping = new ConcurrentHashMap<>();

    this.bundleListener = new RequireJsBundleListener( this );
    this.bundleContext.addBundleListener( this.bundleListener );

    for ( Bundle bundle : this.bundleContext.getBundles() ) {
      this.bundleListener.addBundle( bundle );
    }
  }

  public void destroy() {
    if ( this.requireJsPackageServiceTracker != null ) {
      this.requireJsPackageServiceTracker.close();

      this.requireJsPackageServiceTracker = null;
    }

    if ( this.bundleListener != null ) {
      this.bundleContext.removeBundleListener( this.bundleListener );

      this.bundleListener = null;
    }

    this.invalidateCachedConfigurations();
  }

  public String getRequireJsConfig( String baseUrl, RequireJsConfigServlet.RequestContext requestContext ) {
    // Make sure the baseUrl ends in a slash.
    // like https://github.com/requirejs/requirejs/blob/14526943c937aab3c022235335f20e260395fe15/require.js#L1145
    baseUrl = baseUrl.endsWith( "/" ) ? baseUrl : baseUrl + "/";

    String result = null;
    int tries = 3;
    Exception lastException = null;
    while ( tries-- > 0 && result == null ) {
      Future<String> cache = this.getCachedConfiguration( baseUrl, requestContext );

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

  public String getContextMapping( String baseUrl, RequireJsConfigServlet.RequestContext requestContext ) {
    return this.getCachedContextMapping( baseUrl, requestContext );
  }

  public void invalidateCachedConfigurations() {
    this.cachedConfigurations.forEach( ( s, stringFuture ) -> stringFuture.cancel( true ) );
    this.cachedConfigurations.clear();

    this.cachedContextMapping.clear();
  }

  // package-private for unit testing
  ServiceTracker<RequireJsPackage, RequireJsPackageConfiguration> createRequireJsPackageServiceTracker( BundleContext bundleContext ) {
    return new ServiceTracker<>( bundleContext, RequireJsPackage.class, new RequireJsPackageServiceTracker( bundleContext, this ) );
  }

  Future<String> getCachedConfiguration( String baseUrl, RequireJsConfigServlet.RequestContext requestContext ) {
    return this.cachedConfigurations.computeIfAbsent( baseUrl, k -> {
      List<RequireJsPackageConfiguration> availablePackages = new ArrayList<>();
      Collection<RequireJsPackageConfiguration> requireJsPackageConfigurations = this.requireJsPackageServiceTracker.getTracked().values();
      availablePackages.addAll( requireJsPackageConfigurations );

      return executorService.schedule( new RebuildCacheCallable( baseUrl, availablePackages,
          new ArrayList<>( this.bundleListener.getExternalResourcesRequireJsScripts() ) ), 250, TimeUnit.MILLISECONDS );
    } );
  }

  String getCachedContextMapping( String baseUrl, RequireJsConfigServlet.RequestContext requestContext ) {
    String referer = requestContext.getReferer();

    if ( referer != null ) {
      return this.cachedContextMapping.computeIfAbsent( referer, k -> {
        Collection<RequireJsPackageConfiguration> requireJsPackageConfigurations = this.requireJsPackageServiceTracker.getTracked().values();
        for ( RequireJsPackageConfiguration requireJsPackage : requireJsPackageConfigurations ) {
          if ( referer.contains( baseUrl + requireJsPackage.getWebRootPath() ) ) {
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
}
