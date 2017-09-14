/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
