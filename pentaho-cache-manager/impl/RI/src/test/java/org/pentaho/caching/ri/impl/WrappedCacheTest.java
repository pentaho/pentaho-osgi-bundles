/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.caching.ri.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class WrappedCacheTest {

  public static final String CACHE_NAME = "CACHE_NAME";
  @Rule public ExpectedException thrown = ExpectedException.none();

  private com.google.common.cache.Cache<Object, Object> guavaCache;
  private WrappedCache<Object, Object> cache;

  @Mock private Configuration<Object, Object> configuration;
  @Mock private CacheManager cacheManager;
  @Mock private CacheEntryListenerConfiguration<Object, Object> mockEntryListener;

  @Before
  public void setUp() throws Exception {
    guavaCache = CacheBuilder.newBuilder().build();
    cache = new WrappedCache<Object, Object>( guavaCache ) {

      @Override public String getName() {
        return CACHE_NAME;
      }

      @Override public CacheManager getCacheManager() {
        return cacheManager;
      }

      @Override public <C extends Configuration<Object, Object>> C getConfiguration( Class<C> clazz ) {
        return Constants.unwrap( configuration, clazz );
      }
    };
  }

  @Test
  public void testGet() throws Exception {
    assertThat( cache.get( "some key" ), nullValue() );

    guavaCache.put( "some key", "some value" );
    assertThat( cache.containsKey( "some key" ), is( true ) );
    assertThat( cache.get( "some key" ), equalTo( (Object) "some value" ) );
  }

  @Test
  public void testPut() throws Exception {
    cache.put( "some key", "some value" );

    assertThat( guavaCache.getIfPresent( "some key" ), equalTo( (Object) "some value" ) );
  }

  @Test
  public void testGetAndPut() throws Exception {
    cache.put( "some key", "some value" );

    assertThat( cache.getAndPut( "some key", "other value" ), equalTo( (Object) "some value" ) );
    assertThat( cache.get( "some key" ), equalTo( (Object) "other value" ) );
  }

  @Test
  public void testGetAndPutAll() throws Exception {
    Map<Object, Object> values = ImmutableMap.<Object, Object>of(
      "key1", "value1",
      "key2", "value2"
    );

    cache.putAll( values );
    assertThat( cache.getAll( ImmutableSet.of( "key1", "key2" ) ), equalTo( values ) );
  }

  @Test
  public void testPutIfAbsent() throws Exception {
    assertThat( cache.putIfAbsent( "key", "value" ), is( true ) );
    assertThat( cache.putIfAbsent( "key", "other value" ), is( false ) );

    assertThat( cache.get( "key" ), equalTo( (Object) "value" ) );
  }

  @Test
  public void testRemoveKey() throws Exception {
    assertThat( cache.remove( "key" ), is( false ) );
    assertThat( cache.putIfAbsent( "key", "value" ), is( true ) );
    assertThat( cache.remove( "key" ), is( true ) );
  }

  @Test
  public void testRemoveKeyValue() throws Exception {
    assertThat( cache.remove( "key", "value" ), is( false ) );
    assertThat( cache.putIfAbsent( "key", "value" ), is( true ) );
    assertThat( cache.remove( "key", "other value" ), is( false ) );
    assertThat( cache.remove( "key", "value" ), is( true ) );
    assertThat( cache.remove( "key" ), is( false ) );
  }

  @Test
  public void testGetAndRemove() throws Exception {
    assertThat( cache.putIfAbsent( "key", "value" ), is( true ) );
    assertThat( cache.getAndRemove( "key" ), equalTo( (Object) "value" ) );
    assertThat( cache.getAndRemove( "key" ), nullValue() );
  }

  @Test
  public void testReplace() throws Exception {
    assertThat( cache.replace( "key", "value" ), is( false ) );
    assertThat( cache.putIfAbsent( "key", "value" ), is( true ) );
    assertThat( cache.replace( "key", "value", "other value" ), is( true ) );
    assertThat( cache.getAndReplace( "key", "value" ), equalTo( (Object) "other value" ) );
    assertThat( cache.get( "key" ), equalTo( (Object) "value" ) );
  }

  @Test
  public void testRemoveAll() throws Exception {
    Map<Object, Object> pair1 = ImmutableMap.<Object, Object>of(
      "key1", "value1"
    );
    Map<Object, Object> pair2 = ImmutableMap.<Object, Object>of(
      "key2", "value2"
    );
    ImmutableSet<String> keys = ImmutableSet.of( "key1", "key2" );

    cache.putAll( pair1 );
    cache.putAll( pair2 );

    cache.removeAll( ImmutableSet.of( "key1" ) );
    assertThat( cache.getAll( keys ), equalTo( pair2 ) );
    cache.removeAll();
    assertThat( cache.getAll( keys ).entrySet(), empty() );
  }

  @Test
  public void testClear() throws Exception {
    Map<Object, Object> values = ImmutableMap.<Object, Object>of(
      "key1", "value1",
      "key2", "value2"
    );

    cache.putAll( values );
    assertThat( cache.getAll( values.keySet() ), equalTo( values ) );
    cache.clear();
    assertThat( cache.getAll( values.keySet() ).entrySet(), empty() );
  }

  @Test
  public void testInvoke() throws Exception {
    Map<Object, Object> values = ImmutableMap.<Object, Object>of(
      "key1", "value1",
      "key2", "value2"
    );
    cache.putAll( values );

    Map<Object, EntryProcessorResult<Object>> resultMap =
      cache.invokeAll( values.keySet(), new EntryProcessor<Object, Object, Object>() {
        @Override public Object process( MutableEntry<Object, Object> entry, Object... arguments )
          throws EntryProcessorException {
          if ( entry.getKey().equals( "key1" ) && entry.getValue().equals( "value1" ) ) {
            entry.setValue( "other value" );
            return "result1";
          } else if ( entry.getKey().equals( "key2" ) && entry.getValue().equals( "value2" ) ) {
            entry.remove();
            return "result2";
          }
          throw new AssertionError( "Unexptected entry: " + entry );
        }
      } );

    assertThat( resultMap.get( "key1" ).get(), equalTo( (Object) "result1" ) );
    assertThat( resultMap.get( "key2" ).get(), equalTo( (Object) "result2" ) );

    Cache.Entry<Object, Object> onlyElement = Iterables.getOnlyElement( cache );
    assertThat( onlyElement.getKey(), is( (Object) "key1" ) );
    assertThat( onlyElement.getValue(), is( (Object) "other value" ) );
  }

  @Test
  public void testRegisterCacheEntryListener() throws Exception {
    thrown.expect( CacheException.class );
    thrown.expectMessage( "CacheEntryListeners are not yet supported" );
    cache.registerCacheEntryListener( mockEntryListener );
  }

  @Test
  public void testDeregisterCacheEntryListener() throws Exception {
    thrown.expect( CacheException.class );
    thrown.expectMessage( "CacheEntryListeners are not yet supported" );
    cache.deregisterCacheEntryListener( mockEntryListener );
  }

  @Test
  public void testClose() throws Exception {
    cache.put( "key", "value" );
    assertThat( cache.isClosed(), is( false ) );
    cache.close();
    assertThat( cache.isClosed(), is( true ) );

    thrown.expect( IllegalStateException.class );
    thrown.expectMessage( "Cache is closed" );
    cache.get( "key" );
  }

  @Test
  public void testLoadAll() throws Exception {
    cache.loadAll( ImmutableSet.of( "key1", "key2" ), true, null );
    assertThat( cache, emptyIterable() );
  }
}
