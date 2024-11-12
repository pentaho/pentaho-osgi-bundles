/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.caching.impl.PentahoCacheManagerImpl;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoCacheManagerTest {
  private static final String NAME = "name";
  private final Class<String> keyType = String.class;
  private final Class<Map> valueType = Map.class;

  @Mock private PentahoCacheSystemConfiguration configuration;
  @Mock private PentahoCacheProvidingService service;
  @Mock private CacheManager delegate;
  private PentahoCacheManager cacheManager;

  @Before
  public void setUp() throws Exception {
    when( service.createCacheManager( configuration ) ).thenReturn( delegate );
    cacheManager = new PentahoCacheManagerImpl( configuration, service );
    verify( service ).createCacheManager( configuration );
  }

  @Test
  public void testDelegation() throws Exception {
    cacheManager.close();
    verify( delegate ).close();

    ImmutableMap<String, String> properties = ImmutableMap.of( "propery", "value" );
    cacheManager.createConfiguration( keyType, valueType, properties );
    verify( service ).createConfiguration( keyType, valueType, properties );

    cacheManager.getTemplates();
    verify( configuration ).createTemplates( cacheManager );

    cacheManager.getProperties();
    verify( delegate ).getProperties();

    cacheManager.getCacheNames();
    verify( delegate ).getCacheNames();

    Configuration mockConfiguration = mock( Configuration.class );
    cacheManager.createCache( NAME, mockConfiguration );
    verify( delegate ).createCache( NAME, mockConfiguration );

    cacheManager.getCache( NAME );
    verify( delegate ).getCache( NAME );

    cacheManager.destroyCache( NAME );
    verify( delegate ).destroyCache( NAME );

    cacheManager.getClassLoader();
    verify( delegate ).getClassLoader();

    cacheManager.getURI();
    verify( delegate ).getURI();

    cacheManager.getCachingProvider();
    verify( delegate ).getCachingProvider();

    cacheManager.getCache( NAME, keyType, valueType );
    verify( delegate ).getCache( NAME, keyType, valueType );

    cacheManager.enableManagement( NAME, true );
    verify( delegate ).enableManagement( NAME, true );

    cacheManager.enableStatistics( NAME, true );
    verify( delegate ).enableStatistics( NAME, true );

    assertThat( cacheManager.unwrap( PentahoCacheManagerImpl.class ), is( cacheManager ) );
    cacheManager.unwrap( valueType );
    verify( delegate ).unwrap( valueType );

    cacheManager.isClosed();
    verify( delegate ).isClosed();

    assertThat( cacheManager.getSystemConfiguration(), is( configuration ) );
    assertThat( cacheManager.getService(), is( service ) );
  }
}
