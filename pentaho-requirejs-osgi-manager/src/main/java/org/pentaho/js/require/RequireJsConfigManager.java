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
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bryan on 8/5/14.
 */
public class RequireJsConfigManager {
  public static final String REQUIRE_JSON_PATH = "META-INF/js/require.json";
  private final Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
  private final JSONParser parser = new JSONParser();
  private BundleContext bundleContext;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private volatile Future<String> cache;
  private volatile long lastModified;

  public BundleContext getBundleContext() {
    return bundleContext;
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public boolean updateBundleContext( Bundle bundle ) throws IOException, ParseException {
    boolean shouldInvalidate = updateBundleContextStopped( bundle );
    URL configFileUrl = bundle.getResource( REQUIRE_JSON_PATH );
    if ( configFileUrl == null ) {
      return shouldInvalidate;
    } else {
      URLConnection urlConnection = configFileUrl.openConnection();
      InputStream inputStream = urlConnection.getInputStream();
      InputStreamReader inputStreamReader = null;
      BufferedReader bufferedReader = null;
      StringBuilder sb = new StringBuilder();
      try {
        inputStreamReader = new InputStreamReader( urlConnection.getInputStream() );
        bufferedReader = new BufferedReader( inputStreamReader );
        JSONObject jsonObject = (JSONObject) parser.parse( bufferedReader );
        synchronized( configMap ) {
          configMap.put( bundle.getBundleId(), jsonObject );
        }
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
      return true;
    }
  }

  public boolean updateBundleContextStopped( Bundle bundle ) {
    JSONObject bundleConfig = null;
    synchronized( configMap ) {
      bundleConfig = configMap.remove( bundle.getBundleId() );
    }
    return bundleConfig != null;
  }

  public void invalidateCache( boolean shouldInvalidate ) {
    if ( shouldInvalidate ) {
      synchronized( configMap ) {
        cache = executorService.submit( new RebuildCacheCallable( new HashMap<Long, JSONObject>( this.configMap ) ) );
        lastModified = System.currentTimeMillis();
      }
    }
  }

  public String getRequireJsConfig() {
    Future<String> cache = null;
    String result = null;
    while ( result == null || cache != this.cache ) {
      cache = this.cache;
      try {
        result = cache.get();
      } catch ( InterruptedException e ) {
        // ignore
      } catch ( ExecutionException e ) {
        invalidateCache( true );
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
    bundleContext.addBundleListener( new RequireJsBundleListener( this ) );
    for ( Bundle bundle : bundleContext.getBundles() ) {
      updateBundleContext( bundle );
    }
    updateBundleContext( bundleContext.getBundle() );
    invalidateCache( true );
  }
}
