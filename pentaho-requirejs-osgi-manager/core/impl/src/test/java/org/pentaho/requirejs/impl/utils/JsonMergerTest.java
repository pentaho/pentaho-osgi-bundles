/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class JsonMergerTest {
  private JsonMerger merger;

  private int obj_id;

  @Before
  public void setUp() {
    this.merger = new JsonMerger();

    this.obj_id = 0;
  }

  @Test
  public void mergeObjectsSimpleValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", 1 );
    obj1.put( "b", 1 );
    obj1.put( "c", 1 );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "c", 2 );
    obj2.put( "d", 2 );
    obj2.put( "e", 2 );

    Map<String, Object> result = this.merger.merge( obj1, obj2 );

    assertEquals( 1, result.get( "a" ) );
    assertEquals( 1, result.get( "b" ) );
    assertEquals( 2, result.get( "c" ) );
    assertEquals( 2, result.get( "d" ) );
    assertEquals( 2, result.get( "e" ) );
  }

  @Test
  public void mergeListsSimpleValues() {
    // merge of lists removes duplicates

    List<Object> obj1 = new ArrayList<>();
    obj1.add( 1 );
    obj1.add( 2 );
    obj1.add( 2 );
    obj1.add( 3 );

    List<Object> obj2 = new ArrayList<>();
    obj2.add( 3 );
    obj2.add( 4 );
    obj2.add( 5 );
    obj2.add( 5 );
    obj2.add( 6 );

    List<Object> result = this.merger.merge( obj1, obj2 );

    assertEquals( 6, result.size() );

    assertEquals( 1, result.get( 0 ) );
    assertEquals( 2, result.get( 1 ) );
    assertEquals( 3, result.get( 2 ) );
    assertEquals( 4, result.get( 3 ) );
    assertEquals( 5, result.get( 4 ) );
    assertEquals( 6, result.get( 5 ) );
  }

  @Test
  public void cloneObjectSimpleValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", 1 );
    obj1.put( "b", 2 );
    obj1.put( "c", 3 );

    Map<String, Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );
  }

  @Test
  public void cloneListSimpleValues() {
    List<Object> obj1 = new ArrayList<>();
    obj1.add( 1 );
    obj1.add( 2 );
    obj1.add( 3 );

    List<Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );
  }

  @Test
  public void mergeObjectsComplexValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createObject() );
    obj1.put( "b", createObject() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "b", createObject() );
    obj2.put( "c", createObject() );

    Map<String, Object> result = this.merger.merge( obj1, obj2 );

    Object a = result.get( "a" );
    assertNotSame( obj1.get( "a" ), a );
    assertEquals( obj1.get( "a" ), a );

    Map<String, Object> b = (Map<String, Object>) result.get( "b" );
    assertNotSame( obj1.get( "b" ), b );
    assertNotSame( obj2.get( "b" ), b );
    assertEquals( 5, b.size() );

    Object c = result.get( "c" );
    assertNotSame( obj2.get( "c" ), c );
    assertEquals( obj2.get( "c" ), c );

  }

  @Test
  public void mergeListsComplexValues() {
    List<Object> obj1 = new ArrayList<>();
    obj1.add( createObject() );
    obj1.add( createObject() );

    List<Object> obj2 = new ArrayList<>();
    obj2.add( createObject() );
    obj2.add( createObject() );

    List<Object> result = this.merger.merge( obj1, obj2 );

    assertEquals( 4, result.size() );

    result.forEach( o -> {
      assertNotSame( obj1.get( 0 ), o );
      assertNotSame( obj1.get( 1 ), o );
      assertNotSame( obj2.get( 0 ), o );
      assertNotSame( obj2.get( 1 ), o );
    } );
  }

  @Test
  public void cloneObjectComplexValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createObject() );

    Map<String, Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );

    assertNotSame( obj1.get( "a" ), result.get( "a" ) );
    assertEquals( obj1.get( "a" ), result.get( "a" ) );
  }

  @Test
  public void cloneListComplexValues() {
    List<Object> obj1 = new ArrayList<>();
    obj1.add( createObject() );

    List<Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );

    assertNotSame( obj1.get( 0 ), result.get( 0 ) );
    assertEquals( obj1.get( 0 ), result.get( 0 ) );
  }

  @Test
  public void mergeObjectsListValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createList() );
    obj1.put( "b", createList() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "b", createList() );
    obj2.put( "c", createList() );

    Map<String, Object> result = this.merger.merge( obj1, obj2 );

    Object a = result.get( "a" );
    assertNotSame( obj1.get( "a" ), a );
    assertEquals( obj1.get( "a" ), a );

    List<Object> b = (List<Object>) result.get( "b" );
    assertNotSame( obj1.get( "b" ), b );
    assertNotSame( obj2.get( "b" ), b );
    assertEquals( 4, b.size() );

    Object c = result.get( "c" );
    assertNotSame( obj2.get( "c" ), c );
    assertEquals( obj2.get( "c" ), c );

  }

  @Test
  public void mergeListsListValues() {
    List<Object> obj1 = new ArrayList<>();
    obj1.add( createList() );
    obj1.add( createList() );

    List<Object> obj2 = new ArrayList<>();
    obj2.add( createList() );
    obj2.add( createList() );

    List<Object> result = this.merger.merge( obj1, obj2 );

    assertEquals( 4, result.size() );

    result.forEach( o -> {
      assertNotSame( obj1.get( 0 ), o );
      assertNotSame( obj1.get( 1 ), o );
      assertNotSame( obj2.get( 0 ), o );
      assertNotSame( obj2.get( 1 ), o );
    } );
  }

  @Test
  public void cloneObjectListValues() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createList() );

    Map<String, Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );

    assertNotSame( obj1.get( "a" ), result.get( "a" ) );
    assertEquals( obj1.get( "a" ), result.get( "a" ) );
  }

  @Test
  public void cloneListListValues() {
    List<Object> obj1 = new ArrayList<>();
    obj1.add( createList() );

    List<Object> result = this.merger.clone( obj1 );

    assertNotSame( obj1, result );
    assertEquals( obj1, result );

    assertNotSame( obj1.get( 0 ), result.get( 0 ) );
    assertEquals( obj1.get( 0 ), result.get( 0 ) );
  }

  private Map<String, Object> createObject() {
    Map<String, Object> subobj1 = new HashMap<>();
    subobj1.put( "a", obj_id );
    subobj1.put( "b", obj_id );
    subobj1.put( "c", obj_id );
    subobj1.put( "_" + obj_id, obj_id );

    ++obj_id;

    return subobj1;
  }

  @Test(expected = RuntimeException.class)
  public void mergeObjectsIncompatibleTypes1() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createObject() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", 1 );

    this.merger.merge( obj1, obj2 );
  }

  @Test(expected = RuntimeException.class)
  public void mergeObjectsIncompatibleTypes2() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createList() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", 1 );

    this.merger.merge( obj1, obj2 );
  }

  @Test(expected = RuntimeException.class)
  public void mergeObjectsIncompatibleTypes3() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createObject() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", createList() );

    this.merger.merge( obj1, obj2 );
  }

  @Test(expected = RuntimeException.class)
  public void mergeObjectsIncompatibleTypes4() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", createList() );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", createObject() );

    this.merger.merge( obj1, obj2 );
  }

  @Test(expected = RuntimeException.class)
  public void mergeObjectsIncompatibleTypes5() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", 1 );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", createList() );

    this.merger.merge( obj1, obj2 );
  }

  @Test
  public void mergeObjectsSimpleAlwaysCompatible() {
    Map<String, Object> obj1 = new HashMap<>();
    obj1.put( "a", 1 );
    obj1.put( "b", "string" );
    obj1.put( "c", true );

    Map<String, Object> obj2 = new HashMap<>();
    obj2.put( "a", "a string" );
    obj2.put( "b", 5 );
    obj2.put( "c", 5.6 );

    Map<String, Object> result = this.merger.merge( obj1, obj2 );

    assertEquals( obj2, result );
  }

  private List<Object> createList() {
    List<Object> list = new ArrayList<>( 2 );
    list.add( createObject() );
    list.add( createObject() );

    return list;
  }
}