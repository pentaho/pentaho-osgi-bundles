package org.pentaho.js.require;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nantunes on 12/11/15.
 */
public class RequireJsMerger {
  private JSONObject requireConfig;

  public RequireJsMerger() {
    this.requireConfig = createEmptyRequireConfig();
  }

  public void merge( Map requireConfigPartial ) throws Exception {
    requireConfig = this.merge( requireConfig, requireConfigPartial );
  }

  public JSONObject getRequireConfig() {
    return requireConfig;
  }

  private JSONObject createEmptyRequireConfig() {
    JSONObject emptyConfig = new JSONObject();

    emptyConfig.put( "paths", new JSONObject() );
    emptyConfig.put( "packages", new JSONArray() );
    emptyConfig.put( "bundles", new JSONObject() );

    final JSONObject map = new JSONObject();
    map.put( "*", new JSONObject() );
    emptyConfig.put( "map", map );

    emptyConfig.put( "shim", new JSONObject() );

    final JSONObject config = new JSONObject();
    config.put( "service", new JSONObject() );
    emptyConfig.put( "config", config );

    return emptyConfig;
  }

  private Map toRelativePathedObject( Map jsonObject ) {
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
      return value2 instanceof Map ? toRelativePathedObject( (Map) value2 ) : value2;
    } else if ( value2 == null ) {
      return value1 instanceof Map ? toRelativePathedObject( (Map) value1 ) : value1;
    } else if ( value1 instanceof Map ) {
      if ( value2 instanceof Map ) {
        return merge( (Map) value1, toRelativePathedObject( (Map) value2 ), key.equals( "shim" ) );
      } else {
        throw new Exception( "Cannot merge key " + key + " due to different types" );
      }
    } else if ( value2 instanceof Map ) {
      throw new Exception( "Cannot merge key " + key + " due to different types" );
    } else if ( value1 instanceof List ) {
      if ( value2 instanceof List ) {
        return merge( (List) value1, (List) value2 );
      } else {
        throw new Exception( "Cannot merge key " + key + " due to different types" );
      }
    } else if ( value2 instanceof List ) {
      throw new Exception( "Cannot merge key " + key + " due to different types" );
    } else {
      return value2;
    }
  }

  private JSONArray merge( List array1, List array2 ) {
    Set<Object> hs = new LinkedHashSet<>();
    hs.addAll( array1 );
    hs.addAll( array2 );

    JSONArray result = new JSONArray();
    result.addAll( hs );

    return result;
  }

  private JSONObject merge( Map object1, Map object2 ) throws Exception {
    return this.merge( object1, object2, false );
  }

  private JSONObject merge( Map object1, Map object2, boolean insideShim ) throws Exception {
    Set<String> keys = new HashSet<>( object1.keySet().size() );
    for ( Object key : object1.keySet() ) {
      if ( !( key instanceof String ) ) {
        throw new Exception( "Key " + key + " was not a String" );
      }

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

      if ( insideShim ) {
        if ( value1 instanceof List ) {
          JSONObject deps = new JSONObject();
          deps.put( "deps", value1 );

          value1 = deps;
        }

        if ( value2 instanceof List ) {
          JSONObject deps = new JSONObject();
          deps.put( "deps", value2 );

          value2 = deps;
        }
      }

      result.put( key, merge( key, value1, value2 ) );
    }

    return result;
  }
}
