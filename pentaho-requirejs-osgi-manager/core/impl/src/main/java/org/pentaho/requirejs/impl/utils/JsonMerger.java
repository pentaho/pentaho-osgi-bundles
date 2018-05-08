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
package org.pentaho.requirejs.impl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonMerger {
  public Map<String, Object> merge( Map<String, ?> object1, Map<String, ?> object2 ) {
    Set<String> keys = new HashSet<>( object1.keySet().size() );
    keys.addAll( object1.keySet() );
    keys.addAll( object2.keySet() );

    Map<String, Object> result = new HashMap<>();
    for ( String key : keys ) {
      Object value1 = object1.get( key );
      Object value2 = object2.get( key );

      result.put( key, merge( key, value1, value2 ) );
    }

    return result;
  }

  public List<Object> merge( List<?> array1, List<?> array2 ) {
    Set<Object> hs = new LinkedHashSet<>();

    copyToCollection( array1, hs );
    copyToCollection( array2, hs );

    List<Object> result = new ArrayList<>();
    result.addAll( hs );

    return result;
  }

  public Map<String, Object> clone( Map<String, ?> jsonObject ) {
    return cloneMap( jsonObject );
  }

  public List<Object> clone( List<?> array ) {
    return cloneList( array );
  }

  private Object cloneValue( Object o ) {
    if ( o instanceof Map ) {
      return cloneMap( (Map<String, Object>) o );
    } else if ( o instanceof List ) {
      return cloneList( (List) o );
    }

    return o;
  }

  private Map<String, Object> cloneMap( Map<String, ?> jsonObject ) {
    Map<String, Object> clone = new HashMap<>();

    jsonObject.keySet();

    for ( String key : jsonObject.keySet() ) {
      Object val = jsonObject.get( key );

      if ( val instanceof Map ) {
        clone.put( key, clone( (Map<String, Object>) val ) );
      } else if ( val instanceof List ) {
        clone.put( key, clone( (List<Object>) val ) );
      } else {
        clone.put( key, val );
      }
    }

    return clone;
  }

  private List<Object> cloneList( List<?> array ) {
    List<Object> clone = new ArrayList<>();

    copyToCollection( array, clone );

    return clone;
  }

  private Object merge( String key, Object value1, Object value2 ) {
    if ( value1 == null ) {
      return cloneValue( value2 );
    } else if ( value2 == null ) {
      return cloneValue( value1 );
    } else if ( value1 instanceof Map ) {
      if ( value2 instanceof Map ) {
        return merge( (Map) value1, (Map<String, Object>) value2 );
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
      return cloneValue( value2 );
    }
  }

  private void copyToCollection( List array, Collection<Object> collection ) {
    array.forEach( val -> collection.add( cloneValue( val ) ) );
  }
}
