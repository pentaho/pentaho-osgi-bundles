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
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 8/15/14.
 */
public class RebuildCacheCallableTest {
  private List<RequireJsConfiguration> requireJsConfigurations;

  @Before
  public void setup() {
    requireJsConfigurations = new ArrayList<RequireJsConfiguration>(  );
  }

  @Test
  public void testCall() throws Exception {
    String nullTestKey = "null";
    String arrayKey = "array";
    String objectKey = "object";
    String dupKey = "dup";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    JSONArray array1 = new JSONArray();
    array1.add( 1L );
    array1.add( "s" );
    object1.put( arrayKey, array1 );
    object1.put( nullTestKey, "a" );
    object1.put( objectKey, new JSONObject() );
    object1.put( dupKey, "c" );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    JSONArray array2 = new JSONArray();
    array2.add( "2" );
    array2.add( 3L );
    object2.put( arrayKey, array2 );
    object2.put( nullTestKey, null );
    object2.put( objectKey, new JSONObject() );
    object2.put( dupKey, "e" );

    JSONObject expected = new JSONObject();
    JSONArray expectedArray = new JSONArray();
    expectedArray.addAll( array1 );
    expectedArray.addAll( array2 );
    expected.put( arrayKey, expectedArray );
    expected.put( nullTestKey, "a" );
    expected.put( objectKey, new JSONObject() );
    expected.put( dupKey, "e" );
    expected.put( "shim", new JSONObject(  ) );
    expected.put( "paths", new JSONObject(  ) );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }
    Object configValue = JSONValue.parse( config );
    testEquals( expected, (JSONObject) configValue );
  }

  @Test( expected = Exception.class )
  public void testCannotMergeJSONObjectOtherException() throws Exception {
    String objectKey = "object";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONObject() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    JSONArray array2 = new JSONArray();
    array2.add( "2" );
    array2.add( 3L );
    object2.put( objectKey, array2 );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeArrayJSONObjectException() throws Exception {
    String objectKey = "object";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONArray() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, new JSONObject(  ) );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeArrayOtherException() throws Exception {
    String objectKey = "object";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONArray() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, "B" );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeOtherArrayException() throws Exception {
    String objectKey = "object";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, "B" );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, new JSONArray() );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeObjectKeyException() throws Exception {
    String objectKey = "object";
    Map<Long, JSONObject> configMap = new HashMap<Long, JSONObject>();
    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, "B" );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( new Object(), new JSONArray() );
    String config = new RebuildCacheCallable( configMap, requireJsConfigurations ).call();
  }

  public static void testEquals( Object object1, Object object2 ) {
    if ( object1 instanceof JSONObject && object2 instanceof JSONObject ) {
      testEquals( (JSONObject) object1, (JSONObject) object2 );
    } else if ( object1 instanceof JSONArray && object2 instanceof JSONArray ) {
      testEquals( (JSONArray) object1, (JSONArray) object2 );
    } else {
      assertEquals( object1, object2 );
    }
  }

  private static void testEquals( JSONObject object1, JSONObject object2 ) {
    assertEquals( object1.keySet(), object2.keySet() );
    for ( Object key : object1.keySet() ) {
      testEquals( object1.get( key ), object2.get( key ) );
    }
  }

  private static void testEquals( JSONArray array1, JSONArray array2 ) {
    assertEquals( array1.size(), array2.size() );
    for ( int i = 0; i < array1.size(); i++ ) {
      testEquals( array1.get( i ), array2.get( i ) );
    }
  }
}
