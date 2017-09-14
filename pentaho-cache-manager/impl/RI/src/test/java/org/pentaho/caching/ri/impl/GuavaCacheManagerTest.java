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

package org.pentaho.caching.ri.impl;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
public class GuavaCacheManagerTest {

  public static final String CACHE_NAME = "TYPED_CACHE";

  @Rule public ExpectedException thrown = ExpectedException.none();

  private GuavaCacheManager cacheManager;

  @Before
  public void setUp() throws Exception {
    cacheManager = new GuavaCacheManager();
  }

  @Test
  public void testExpiry() throws Exception {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    MutableConfiguration<String, String> configuration = new MutableConfiguration<String, String>();

    final AtomicInteger elapsedTime = new AtomicInteger( 0 );
    cacheBuilder.ticker( new Ticker() {
      @Override public long read() {
        return TimeUnit.SECONDS.toNanos( elapsedTime.get() );
      }
    } );

    configuration.setExpiryPolicyFactory( TouchedExpiryPolicy.factoryOf( new Duration( TimeUnit.MINUTES, 1 ) ) );

    cacheManager.configureCacheBuilder( configuration, cacheBuilder );

    ConcurrentMap<String, String> cache = cacheBuilder.<String, String>build().asMap();

    elapsedTime.set( 0 );
    cache.put( "key", "value" );

    elapsedTime.addAndGet( 45 );
    assertThat( cache.replace( "key", "value", "new value" ), is( true ) );

    elapsedTime.addAndGet( 45 );
    assertThat( cache.get( "key" ), equalTo( "new value" ) );

    elapsedTime.addAndGet( 45 );
    assertThat( cache.get( "key" ), nullValue() );
  }

  @Test
  public void testNewCache() throws Exception {
    MutableConfiguration<String, Map> configuration = new MutableConfiguration<String, Map>();
    configuration.setTypes( String.class, Map.class );
    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );

    assertThat( cache.getConfiguration( Configuration.class ), sameInstance( (Configuration) configuration ) );
    assertThat( cache.getName(), is( CACHE_NAME ) );
    assertThat( cacheManager.getCacheNames(), contains( CACHE_NAME ) );

    cache.close();

    assertThat( cache.isClosed(), is( true ) );
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
  }
}
