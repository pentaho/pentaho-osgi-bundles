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
package org.pentaho.osgi.blueprint.collection.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 4/15/16.
 */
public class RankedListTest {
  private RankedList<String> rankedList;

  @Before
  public void setup() {
    rankedList = new RankedList<>( String::compareTo );
  }

  @Test
  public void testRankedList() {
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";
    String e = "e";
    String f = "f";
    rankedList.itemAdded( a, Collections.singletonMap( RankedList.SERVICE_RANKING, 10 ) );
    rankedList.itemAdded( b, Collections.emptyMap() );
    rankedList.itemAdded( c, Collections.singletonMap( RankedList.SERVICE_RANKING, "12" ) );
    rankedList.itemAdded( d, Collections.singletonMap( RankedList.SERVICE_RANKING, new Object() {
      @Override public String toString() {
        return "11";
      }
    } ) );
    rankedList.itemAdded( null, Collections.emptyMap() );
    rankedList.itemAdded( e, Collections.singletonMap( RankedList.SERVICE_RANKING, 10 ) );
    rankedList.itemAdded( f, null );
    assertEquals( new ArrayList<>( Arrays.asList( c, d, a, e, b, f ) ), rankedList.getList() );
    rankedList.itemRemoved( null );
    assertEquals( new ArrayList<>( Arrays.asList( c, d, a, e, b, f ) ), rankedList.getList() );
    rankedList.itemRemoved( a );
    assertEquals( new ArrayList<>( Arrays.asList( c, d, e, b, f ) ), rankedList.getList() );
  }

  @Test
  public void testRankedItemToString() {
    assertEquals( "(100) test", rankedList.getToString( 100, "test" ) );
  }
}
