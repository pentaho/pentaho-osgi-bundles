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
