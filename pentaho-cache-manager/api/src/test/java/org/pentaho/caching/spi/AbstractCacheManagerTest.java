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

package org.pentaho.caching.spi;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author nhudak
 */
@SuppressWarnings( "unchecked" )
public class AbstractCacheManagerTest {

  public static final String CACHE_NAME = "TYPED_CACHE";
  public static final String ANONYMOUS_TYPE_CACHE = "ANONYMOUS_TYPE_CACHE";

  @Rule public ExpectedException thrown = ExpectedException.none();
  private AbstractCacheManager cacheManager;
  private List<Cache> mockCaches;

  @Before
  public void setUp() throws Exception {
    mockCaches = Lists.newArrayList();
    cacheManager = new AbstractCacheManager() {
      @Override
      public <K, V, C extends Configuration<K, V>> Cache<K, V> newCache( String cacheName, C configuration ) {
        Cache cache = mock( Cache.class );
        when( cache.getConfiguration( any( Class.class ) ) ).thenReturn( configuration );
        when( cache.getName() ).thenReturn( cacheName );
        mockCaches.add( cache );
        return ( (Cache<K, V>) cache );
      }
    };
  }

  @Test
  public void testCreateCache() throws Exception {
    MutableConfiguration<String, Map> configuration = new MutableConfiguration<String, Map>();
    configuration.setTypes( String.class, Map.class );
    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );
    assertThat( cache.getConfiguration( Configuration.class ), sameInstance( (Configuration) configuration ) );
    assertThat( cache.getName(), is( CACHE_NAME ) );
    assertThat( mockCaches, hasSize( 1 ) );

    try {
      cacheManager.createCache( CACHE_NAME, configuration );
      fail( "Exception should have been thrown" );
    } catch ( Throwable t ) {
      assertThat( t, instanceOf( CacheException.class ) );
    }

    assertThat( mockCaches, hasSize( 2 ) );
    verify( mockCaches.get( 1 ) ).close();
  }

  @Test
  public void testGetCache() throws Exception {
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
    assertThat( cacheManager.getCache( CACHE_NAME, String.class, Map.class ), nullValue() );

    MutableConfiguration<String, Map> configuration = new MutableConfiguration<String, Map>();
    configuration.setTypes( String.class, Map.class );

    Cache<String, Map> typedCache = cacheManager.createCache( CACHE_NAME, configuration );
    Cache anonymousCache = cacheManager.createCache( ANONYMOUS_TYPE_CACHE, new MutableConfiguration<Object, Object>() );

    assertThat( cacheManager.getCache( CACHE_NAME, String.class, Map.class ), sameInstance( typedCache ) );
    assertThat( cacheManager.getCache( ANONYMOUS_TYPE_CACHE ), sameInstance( anonymousCache ) );

    assertThat( cacheManager.getCacheNames(), containsInAnyOrder( ANONYMOUS_TYPE_CACHE, CACHE_NAME ) );

    thrown.expect( IllegalArgumentException.class );
    cacheManager.getCache( CACHE_NAME, Integer.class, List.class );
  }

  @Test
  public void testDestroyCache() throws Exception {
    cacheManager.destroyCache( CACHE_NAME );

    Cache<Object, Object> cache = cacheManager.createCache( CACHE_NAME, new MutableConfiguration<Object, Object>() );
    assertThat( cacheManager.getCacheNames(), contains( CACHE_NAME ) );

    cacheManager.destroyCache( CACHE_NAME );
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
    verify( cache, only() ).close();
  }

  @Test
  public void testClose() throws Exception {
    int numCaches = 10;
    List<Cache> caches = Lists.newArrayListWithExpectedSize( numCaches );
    for ( int i = 0; i < numCaches; i++ ) {
      caches.add( cacheManager.createCache( "cache_" + i, new MutableConfiguration() ) );
    }

    assertThat( cacheManager.isClosed(), is( false ) );

    cacheManager.close();

    assertThat( cacheManager.isClosed(), is( true ) );
    for ( Cache cache : caches ) {
      verify( cache, only() ).close();
    }
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
  }
}
