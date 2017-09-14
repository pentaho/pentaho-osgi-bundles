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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author tkafalas 6/21/2017
 */
public class ServiceMapTest {
  private ServiceMap<String> serviceMap;

  private final String KEY1 = "key1";
  private final String KEY2 = "key2";
  private final String VALUE1 = "value1";
  private final String VALUE2 = "value2";

  @Before
  public void setup() {
    serviceMap = new ServiceMap<String>();
    addItem( KEY1, VALUE1 );
    addItem( KEY2, VALUE2 );
  }

  @Test
  public void testGetItem() {
    assertEquals( VALUE1, serviceMap.getItem( KEY1 ) );
    assertEquals( VALUE2, serviceMap.getItem( KEY2 ) );
  }

  @Test
  public void testItemRemoved() {
    serviceMap.itemRemoved( VALUE1, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, KEY1 ) );
    assertNull( VALUE1, serviceMap.getItem( KEY1 ) );
    assertEquals( VALUE2, serviceMap.getItem( KEY2 ) );

    // Should not throw an exception
    serviceMap.itemRemoved( null, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, "keyNotThere" ) );
  }

  @Test
  public void testGetMap() {
    Map<String, String> map = serviceMap.getMap();
    assertEquals( VALUE1, map.get( KEY1 ) );
    assertEquals( VALUE2, map.get( KEY2 ) );
  }

  private void addItem( String key, String value ) {
    serviceMap.itemAdded( value, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, key ) );
  }

  @Test
  public void testGetToString() {
    assertEquals( "(" + KEY1 + ") " + VALUE1, serviceMap.getToString( KEY1, VALUE1 ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testServiceMapKeyException() {
    serviceMap = new ServiceMap<String>();
    serviceMap.itemAdded( VALUE1, ImmutableMap.of() );
  }
}
