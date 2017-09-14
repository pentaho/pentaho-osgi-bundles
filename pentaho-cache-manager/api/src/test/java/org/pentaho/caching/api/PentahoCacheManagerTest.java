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

package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
