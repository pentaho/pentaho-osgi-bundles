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

package org.pentaho.caching.ehcache;

import org.junit.Test;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;

import javax.cache.CacheManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EhcacheProvidingServiceTest {

  private EhcacheProvidingService service = new EhcacheProvidingService();

  @Test public void testCreateCacheManager() throws Exception {
    CacheManager cacheManager = service.createCacheManager( mock( PentahoCacheSystemConfiguration.class ) );
    assertNotNull( cacheManager );
    try {
      cacheManager.unwrap( net.sf.ehcache.CacheManager.class );
    } catch ( IllegalArgumentException iae ) {
      fail( "Expected CacheManager to be backed by ehcache CacheManager." );
    }
  }
}
