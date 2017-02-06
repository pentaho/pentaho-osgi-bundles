/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

public class RebuildCacheCallableTest {
  private final String baseUrl = "/some/base/path";

  private List<RequireJsConfiguration> requireJsConfigurations;

  private static void testEquals( Object object1, Object object2 ) {
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

  @Before
  public void setup() {
    this.requireJsConfigurations = new ArrayList<>();
  }

  @Test
  public void testCall() throws Exception {
    String nullTestKey = "null";
    String arrayKey = "array";
    String objectKey = "object";
    String dupKey = "dup";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

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
    expected.put( "bundles", new JSONObject() );
    expected.put( "packages", new JSONArray() );
    JSONObject map = new JSONObject();
    map.put( "*", new JSONObject() );
    expected.put( "map", map );
    expected.put( arrayKey, expectedArray );
    expected.put( nullTestKey, "a" );
    expected.put( objectKey, new JSONObject() );
    expected.put( dupKey, "e" );
    expected.put( "shim", new JSONObject() );
    expected.put( "paths", new JSONObject() );
    JSONObject configObj = new JSONObject();
    configObj.put( "service", new JSONObject() );
    expected.put( "config", configObj );
    String config = new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }
    Object configValue = JSONValue.parse( config );
    testEquals( expected, (JSONObject) configValue );
  }

  @Test( expected = Exception.class )
  public void testCannotMergeJSONObjectOtherException() throws Exception {
    String objectKey = "object";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONObject() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    JSONArray array2 = new JSONArray();
    array2.add( "2" );
    array2.add( 3L );
    object2.put( objectKey, array2 );
    new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeArrayJSONObjectException() throws Exception {
    String objectKey = "object";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONArray() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, new JSONObject() );
    new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
  }

  @Test
  public void testCanMergeArrayJSONObjectIfKeyIsShim() throws Exception {
    String objectKey = "shim";
    String moduleKey = "moduleA";
    String subobjectKey = "deps";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    object1.put( moduleKey, new JSONArray() );

    JSONObject shim1 = new JSONObject();
    shim1.put( objectKey, object1 );

    configMap.put( 1L, shim1 );

    JSONObject subobject2 = new JSONObject();
    subobject2.put( subobjectKey, new JSONArray() );

    JSONObject object2 = new JSONObject();
    object2.put( moduleKey, subobject2 );

    JSONObject shim2 = new JSONObject();
    shim2.put( objectKey, object2 );

    configMap.put( 2L, shim2 );

    String config = new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }
    Object configValue = JSONValue.parse( config );
    testEquals( new JSONArray(), ( (JSONObject) ( (JSONObject) ( (JSONObject) configValue ).get( objectKey ) ).get( moduleKey ) ).get( subobjectKey ) );
  }

  @Test( expected = Exception.class )
  public void testCannotMergeArrayOtherException() throws Exception {
    String objectKey = "object";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, new JSONArray() );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, "B" );
    new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeOtherArrayException() throws Exception {
    String objectKey = "object";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, "B" );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( objectKey, new JSONArray() );
    new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
  }

  @Test( expected = Exception.class )
  public void testCannotMergeObjectKeyException() throws Exception {
    String objectKey = "object";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );
    object1.put( objectKey, "B" );
    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );
    object2.put( new Object(), new JSONArray() );
    new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
  }

  @Test
  public void testRelativePathConversion() throws Exception {
    String objectKey = "object";
    String moduleKey = "module";
    String modulePath = "module/path/script";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );

    JSONObject relativePath = new JSONObject();
    relativePath.put( moduleKey, modulePath );

    object1.put( objectKey, relativePath );

    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );

    JSONObject absolutePath = new JSONObject();
    absolutePath.put( moduleKey, "/" + modulePath );

    object2.put( objectKey, absolutePath );

    String config = new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }
    Object configValue = JSONValue.parse( config );
    testEquals( modulePath, ( (JSONObject) ( (JSONObject) configValue ).get( objectKey ) ).get( moduleKey ) );
  }

  @Test
  public void testOutputAbsolutePaths() throws Exception {
    String relativePath = "module/relative-path/script";
    String absolutePath = "/module/absolute-path/script";

    Map<Long, Map<String, Object>> configMap = new HashMap<>();

    JSONObject object1 = new JSONObject();
    configMap.put( 1L, object1 );

    JSONObject pathDefinition = new JSONObject();
    pathDefinition.put( "module1", relativePath );
    pathDefinition.put( "module2", absolutePath );

    object1.put( "paths", pathDefinition );

    JSONObject object2 = new JSONObject();
    configMap.put( 2L, object2 );

    final JSONArray packagesArray = new JSONArray();

    JSONObject packageDefinition = new JSONObject();
    packageDefinition.put( "name", "package1" );
    packageDefinition.put( "location", relativePath );

    packagesArray.add( packageDefinition );

    packageDefinition = new JSONObject();
    packageDefinition.put( "name", "package2" );
    packageDefinition.put( "location", absolutePath );

    packagesArray.add( packageDefinition );

    object2.put( "packages", packagesArray );

    String config = new RebuildCacheCallable( this.baseUrl, configMap, this.requireJsConfigurations ).call();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }

    System.out.print( config );
    Object configValue = JSONValue.parse( config );

    // check that the baseUrl is prepended to paths
    // (both relative and absolute, because of path earlier conversion)
    testEquals( this.baseUrl + "/" + relativePath, ( (JSONObject) ( (JSONObject) configValue ).get( "paths" ) ).get( "module1" ) );
    testEquals( this.baseUrl + absolutePath, ( (JSONObject) ( (JSONObject) configValue ).get( "paths" ) ).get( "module2" ) );

    // check that the baseUrl is prepended to package locations
    testEquals( this.baseUrl + "/" + relativePath, ( (JSONObject) ( (JSONArray) ( (JSONObject) configValue ).get( "packages" ) ).get( 0 )).get( "location" ) );
    testEquals( absolutePath, ( (JSONObject) ( (JSONArray) ( (JSONObject) configValue ).get( "packages" ) ).get( 1 )).get( "location" ) );
  }
}
