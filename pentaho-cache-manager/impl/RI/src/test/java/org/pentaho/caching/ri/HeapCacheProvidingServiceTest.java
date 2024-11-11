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


package org.pentaho.caching.ri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.ri.impl.GuavaCacheManager;

import javax.cache.CacheManager;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class HeapCacheProvidingServiceTest {

  @Mock private PentahoCacheSystemConfiguration config;
  private HeapCacheProvidingService service;

  @Before
  public void setUp() throws Exception {
    service = new HeapCacheProvidingService();
  }

  @Test
  public void testCreateCacheManager() throws Exception {
    CacheManager cacheManager = service.createCacheManager( config );
    assertThat( cacheManager, instanceOf( GuavaCacheManager.class ) );
  }
}
