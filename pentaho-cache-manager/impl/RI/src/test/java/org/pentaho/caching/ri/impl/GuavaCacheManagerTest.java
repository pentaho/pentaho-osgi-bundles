package org.pentaho.caching.ri.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
public class GuavaCacheManagerTest {

  public static final String CACHE_NAME = "cache name";

  @Rule public ExpectedException thrown = ExpectedException.none();

  private GuavaCacheManager cacheManager;

  private MutableConfiguration<String, Map> configuration;

  @Before
  public void setUp() throws Exception {
    cacheManager = new GuavaCacheManager();

    configuration = new MutableConfiguration<String, Map>();
    configuration.setTypes( String.class, Map.class );
  }

  @Test
  public void testCreateCache() throws Exception {
    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );
    assertThat( cache.getConfiguration( Configuration.class ), equalTo( (Configuration) configuration ) );
    assertThat( cache.getName(), is( CACHE_NAME ) );

    thrown.expect( IllegalArgumentException.class );
    cacheManager.createCache( CACHE_NAME, configuration );
  }

  @Test
  public void testGetCache() throws Exception {
    assertThat( cacheManager.getCacheNames(), not( contains( CACHE_NAME ) ) );

    Cache<String, Map> cache = cacheManager.getCache( CACHE_NAME, String.class, Map.class );
    assertThat( cache.getName(), is( CACHE_NAME ) );
    assertThat( cacheManager.getCache( CACHE_NAME, String.class, Map.class ), sameInstance( cache ) );

    String anonymous_type_cache = "ANONYMOUS_TYPE_CACHE";
    cacheManager.getCache( anonymous_type_cache );

    assertThat( cacheManager.getCacheNames(), containsInAnyOrder( anonymous_type_cache, CACHE_NAME ) );

    thrown.expect( IllegalArgumentException.class );
    cacheManager.getCache( CACHE_NAME );
  }

  @Test
  public void testDestroyCache() throws Exception {
    cacheManager.destroyCache( CACHE_NAME );

    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );
    assertThat( cacheManager.getCacheNames(), contains( CACHE_NAME ) );
    assertThat( cache.isClosed(), is( false ) );

    cacheManager.destroyCache( CACHE_NAME );
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
    assertThat( cache.isClosed(), is( true ) );
  }

  @Test
  public void testClose() throws Exception {
    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );

    assertThat( cache.isClosed(), is( false ) );
    assertThat( cacheManager.isClosed(), is( false ) );

    cacheManager.close();

    assertThat( cache.isClosed(), is( true ) );
    assertThat( cacheManager.isClosed(), is( true ) );
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
  }
}
