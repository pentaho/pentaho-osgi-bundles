/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
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

  @Test
  public void testConcurrency() throws InterruptedException {

    int iterations = 100;
    int numThreads = 100;

    ImmutableMap<String, String> items = ImmutableMap.of( KEY1, VALUE1, KEY2, VALUE2 );
    CountDownLatch countDown = new CountDownLatch( numThreads );
    AtomicInteger atomicInteger = new AtomicInteger( 0 );

    ExecutorService threadPool = Executors.newFixedThreadPool( numThreads );
    Runnable exec = () -> {
      for ( int i = 0; i < iterations; i++ ) {
        serviceMap.itemAdded( VALUE1, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, KEY1 ) );
        serviceMap.itemAdded( VALUE2, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, KEY2 ) );
        try {
          serviceMap.getItem( KEY1 );
          serviceMap.getItem( KEY2 );
        } catch ( NullPointerException npe ) {
          atomicInteger.incrementAndGet();
        }

        serviceMap.itemRemoved( VALUE1, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, KEY1 ) );
        serviceMap.itemRemoved( VALUE2, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, KEY2 ) );
      }
      countDown.countDown();
    };
    for ( int i = 0; i < numThreads; i++ ) {
      threadPool.execute( exec );
    }
    countDown.await( 5000, TimeUnit.MILLISECONDS );
    Assert.assertEquals( 0, atomicInteger.get() );
  }

  @Test
  public void nullKeyIsAllowed() {
    assertNull( serviceMap.getItem( null ) );
  }

}
