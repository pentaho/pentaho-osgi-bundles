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

import org.json.simple.JSONArray;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bryan on 8/5/14.
 */
public class RequireJsConfigManager {
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
    URL configFileUrl = bundle.getResource( "META-INF/js/require.json" );
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
        synchronized ( configMap ) {
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
    synchronized ( configMap ) {
      bundleConfig = configMap.remove( bundle.getBundleId() );
    }
    if ( bundleConfig == null ) {
      return false;
    }
    return true;
  }

  public void invalidateCache( boolean shouldInvalidate ) {
    if ( shouldInvalidate ) {
      synchronized ( configMap ) {
        final Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>( this.configMap );
        cache = executorService.submit( new Callable<String>() {
          private Object merge( String key, Object value1, Object value2 ) throws Exception {
            if ( value1 == null ) {
              return value2;
            } else if ( value2 == null ) {
              return value1;
            } else {
              if ( value1 instanceof JSONObject ) {
                if ( value2 instanceof JSONObject ) {
                  return merge( (JSONObject) value1, (JSONObject) value2 );
                } else {
                  throw new Exception( "Cannot merge key " + key + " due to different types." );
                }
              } else if ( value1 instanceof JSONObject ) {
                throw new Exception( "Cannot merge key " + key + " due to different types." );
              } else if ( value1 instanceof JSONArray ) {
                if ( value2 instanceof JSONArray ) {
                  return merge( (JSONArray) value1, (JSONArray) value2 );
                } else {
                  throw new Exception( "Cannot merge key " + key + " due to different types." );
                }
              } else if ( value2 instanceof JSONArray ) {
                throw new Exception( "Cannot merge key " + key + " due to different types." );
              } else {
                //TODO Should we warn here?
                return value2;
              }
            }
          }

          private JSONArray merge( JSONArray array1, JSONArray array2 ) {
            JSONArray result = new JSONArray();
            result.addAll( array1 );
            result.addAll( array2 );
            return result;
          }

          private JSONObject merge( JSONObject object1, JSONObject object2 ) throws Exception {
            Set<String> keys = new HashSet<String>( object1.keySet().size() );
            for ( Object key : object1.keySet() ) {
              keys.add( (String) key );
            }
            for ( Object key : object2.keySet() ) {
              if ( !( key instanceof String ) ) {
                throw new Exception( "Key " + key + " was not a String" );
              }
              keys.add( (String) key );
            }
            JSONObject result = new JSONObject();
            for ( String key : keys ) {
              Object value1 = object1.get( key );
              Object value2 = object2.get( key );
              result.put( key, merge( key, value1, value2 ) );
            }
            return result;
          }

          @Override
          public String call() throws Exception {
            List<Long> bundleIds = new ArrayList<Long>( configMap.keySet() );
            Collections.sort( bundleIds );
            JSONObject result = new JSONObject();
            for ( Long bundleId : bundleIds ) {
              result = merge( result, configMap.get( bundleId ) );
            }
            return result.toJSONString();
          }
        } );
        lastModified = System.currentTimeMillis();
      }
    }
  }

  public String getRequireJsConfig() {
    Future<String> cache = null;
    String result = null;
    do {
      cache = this.cache;
      try {
        result = cache.get();
      } catch ( InterruptedException e ) {
        //TODO
        e.printStackTrace();
      } catch ( ExecutionException e ) {
        //TODO
        e.printStackTrace();
      }
    } while ( result == null || cache != this.cache );
    return result;
  }

  public long getLastModified() {
    return lastModified;
  }

  public void init() throws Exception {
    BundleListener listener = new BundleListener() {
      @Override
      public void bundleChanged( BundleEvent event ) {
        switch( event.getType() ) {
          case BundleEvent.STARTED:
            //case BundleEvent.UPDATED:
            try {
              invalidateCache( updateBundleContext( event.getBundle() ) );
            } catch ( IOException e ) {
              //TODO
              e.printStackTrace();
            } catch ( ParseException e ) {
              //TODO
              e.printStackTrace();
            }
            break;
          case BundleEvent.STOPPED:
            invalidateCache( updateBundleContextStopped( event.getBundle() ) );
            break;
        }
      }
    };
    bundleContext.addBundleListener( listener );
    for ( Bundle bundle : bundleContext.getBundles() ) {
      updateBundleContext( bundle );
    }
    updateBundleContext( bundleContext.getBundle() );
    invalidateCache( true );
  }
}
