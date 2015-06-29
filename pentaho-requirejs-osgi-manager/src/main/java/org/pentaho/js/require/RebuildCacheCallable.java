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
import org.osgi.framework.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by bryan on 8/15/14.
 */
public class RebuildCacheCallable implements Callable<String> {
  private final Map<Long, JSONObject> configMap;
  private final List<RequireJsConfiguration> requireJsConfigurations;

  public RebuildCacheCallable( Map<Long, JSONObject> configMap, List<RequireJsConfiguration> requireJsConfigurations ) {
    this.configMap = configMap;
    this.requireJsConfigurations = new ArrayList<RequireJsConfiguration>( requireJsConfigurations );
    Collections.sort( this.requireJsConfigurations, new Comparator<RequireJsConfiguration>() {
      @Override public int compare( RequireJsConfiguration o1, RequireJsConfiguration o2 ) {
        long longResult = o1.getBundle().getBundleId() - o2.getBundle().getBundleId();
        if ( longResult < 0L ) {
          return -1;
        } else if ( longResult > 0L ) {
          return 1;
        } else {
          return 0;
        }
      }
    } );
  }

  private JSONObject toRelativePathedObject( JSONObject jsonObject ) {

    jsonObject.keySet();
    for ( Object key : jsonObject.keySet() ) {
      Object val = jsonObject.get( key );
      if ( val instanceof String ) {
        String strVal = (String) val;
        if ( strVal.startsWith( "/" ) ) {
          strVal = strVal.substring( 1 );
          jsonObject.put( key, strVal );
        }
      }

    }
    return jsonObject;
  }

  private Object merge( String key, Object value1, Object value2 ) throws Exception {
    if ( value1 == null ) {
      return value2 instanceof JSONObject ? toRelativePathedObject( (JSONObject) value2 ) : value2;
    } else if ( value2 == null ) {
      return value2 instanceof JSONObject ? toRelativePathedObject( (JSONObject) value1 ) : value1;
    } else {
      if ( value1 instanceof JSONObject ) {
        if ( value2 instanceof JSONObject ) {
          return merge( (JSONObject) value1, (JSONObject) value2 );
        } else {
          throw new Exception( "Cannot merge key " + key + " due to different types." );
        }
      } else if ( value2 instanceof JSONObject ) {
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
    if ( !result.containsKey( "paths" ) ) {
      result.put( "paths", new JSONObject() );
    }
    if ( !result.containsKey( "shim" ) ) {
      result.put( "shim", new JSONObject() );
    }
    if ( !result.containsKey( "map" ) ) {
      result.put( "map", new JSONObject() );
    }
    Object mapObj = result.get( "map" );
    if ( mapObj instanceof Map ) {
      Map map = (Map) mapObj;
      if ( !map.containsKey( "*" ) ) {
        map.put( "*", new JSONObject() );
      }
    }
    if ( !result.containsKey( "bundles" ) ) {
      result.put( "bundles", new JSONObject() );
    }
    if ( !result.containsKey( "config" ) ) {
      result.put( "config", new JSONObject() );
    }
    Object configObj = result.get( "config" );
    if ( configObj instanceof Map ) {
      Map configMap = (Map) configObj;
      if ( !configMap.containsKey( "service" ) ) {
        configMap.put( "service", new JSONObject() );
      }
    }
    if ( !result.containsKey( "packages" ) ) {
      result.put( "packages", new JSONArray() );
    }
    StringBuilder sb = new StringBuilder( result.toJSONString() );
    sb.append( ";" );
    for ( RequireJsConfiguration requireJsConfiguration : requireJsConfigurations ) {
      sb.append( "\n\n/* Following configurations are from bundle " );
      Bundle bundle = requireJsConfiguration.getBundle();
      StringBuilder bundleNameSb = new StringBuilder( "[" );
      bundleNameSb.append( bundle.getBundleId() );
      bundleNameSb.append( "] - " );
      bundleNameSb.append( bundle.getSymbolicName() );
      bundleNameSb.append( ":" );
      bundleNameSb.append( bundle.getVersion() );
      String bundleName = bundleNameSb.toString();
      sb.append( bundleName );
      sb.append( "*/\n" );
      for ( String config : requireJsConfiguration.getRequireConfigurations() ) {
        URL configURL = bundle.getResource( config );
        URLConnection urlConnection = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
          urlConnection = configURL.openConnection();
          inputStream = urlConnection.getInputStream();
          inputStreamReader = new InputStreamReader( inputStream );
          bufferedReader = new BufferedReader( inputStreamReader );
          String input = null;
          while ( ( input = bufferedReader.readLine() ) != null ) {
            sb.append( input );
            sb.append( "\n" );
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
      }
      sb.append( "/* End of bundle " );
      sb.append( bundleName );
      sb.append( "*/\n" );
    }

    return sb.toString();
  }
}
