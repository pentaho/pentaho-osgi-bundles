package org.pentaho.js.require;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nantunes on 12/11/15.
 */
public class RequireJsMerger {
  private Map<String, Object> requireConfig;

  public RequireJsMerger() {
    this.requireConfig = createEmptyRequireConfig();
  }

  public void merge( Map<String, Object> requireConfigPartial ) {
    requireConfig = this.merge( requireConfig, requireConfigPartial );
  }

  public Map<String, Object> getRequireConfig() {
    return requireConfig;
  }

  private Map<String, Object> createEmptyRequireConfig() {
    Map<String, Object> emptyConfig = new HashMap<>();

    emptyConfig.put( "paths", new HashMap<String, Object>() );
    emptyConfig.put( "packages", new ArrayList<>() );
    emptyConfig.put( "bundles", new HashMap<String, Object>() );

    final Map<String, Object> map = new HashMap<>();
    map.put( "*", new HashMap<String, Object>() );
    emptyConfig.put( "map", map );

    emptyConfig.put( "shim", new HashMap<String, Object>() );

    final Map<String, Object> config = new HashMap<>();
    config.put( "service", new HashMap<String, Object>() );
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

  private Object merge( String key, Object value1, Object value2 ) {
    if ( value1 == null ) {
      return value2 instanceof Map ? toRelativePathedObject( (Map) value2 ) : value2;
    } else if ( value2 == null ) {
      return value1 instanceof Map ? toRelativePathedObject( (Map) value1 ) : value1;
    } else if ( value1 instanceof Map ) {
      if ( value2 instanceof Map ) {
        return merge( (Map) value1, toRelativePathedObject( (Map) value2 ), key.equals( "shim" ) );
      } else {
        throw new RuntimeException( "Cannot merge key " + key + " due to different types" );
      }
    } else if ( value2 instanceof Map ) {
      throw new RuntimeException( "Cannot merge key " + key + " due to different types" );
    } else if ( value1 instanceof List ) {
      if ( value2 instanceof List ) {
        return merge( (List) value1, (List) value2 );
      } else {
        throw new RuntimeException( "Cannot merge key " + key + " due to different types" );
      }
    } else if ( value2 instanceof List ) {
      throw new RuntimeException( "Cannot merge key " + key + " due to different types" );
    } else {
      return value2;
    }
  }

  private List<Object> merge( List array1, List array2 ) {
    Set<Object> hs = new LinkedHashSet<>();
    hs.addAll( array1 );
    hs.addAll( array2 );

    List<Object> result = new ArrayList<>();
    result.addAll( hs );

    return result;
  }

  private Map<String, Object> merge( Map<String, Object> object1, Map<String, Object> object2 ) {
    return this.merge( object1, object2, false );
  }

  private Map<String, Object> merge( Map<String, Object> object1, Map<String, Object> object2, boolean insideShim ) {
    Set<String> keys = new HashSet<>( object1.keySet().size() );
    for ( Object key : object1.keySet() ) {
      if ( !( key instanceof String ) ) {
        throw new RuntimeException( "Key " + key + " was not a String" );
      }

      keys.add( (String) key );
    }

    for ( Object key : object2.keySet() ) {
      if ( !( key instanceof String ) ) {
        throw new RuntimeException( "Key " + key + " was not a String" );
      }

      keys.add( (String) key );
    }

    Map<String, Object> result = new HashMap<>();
    for ( String key : keys ) {
      Object value1 = object1.get( key );
      Object value2 = object2.get( key );

      if ( insideShim ) {
        if ( value1 instanceof List ) {
          Map<String, Object> deps = new HashMap<>();
          deps.put( "deps", value1 );

          value1 = deps;
        }

        if ( value2 instanceof List ) {
          Map<String, Object> deps = new HashMap<>();
          deps.put( "deps", value2 );

          value2 = deps;
        }
      }

      result.put( key, merge( key, value1, value2 ) );
    }

    return result;
  }
}
