/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
