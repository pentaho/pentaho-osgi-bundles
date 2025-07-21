/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024-2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.caching.ehcache;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.spi.AbstractCacheProvidingService;

/**
 *  PentahoCacheProvidingService implementation which leverages the
 *  org.ehcache.jcache project to provide a JCache CacheManager wrapping
 *  ehcache 2.x. Once we move to ehcache 3.x we'll no longer need the wrapper.
 */
public class EhcacheProvidingService extends AbstractCacheProvidingService {

  private static CacheManager cacheManager;


  @Override public javax.cache.CacheManager createCacheManager( PentahoCacheSystemConfiguration systemConfiguration ) {
    return Caching.getCachingProvider( this.getClass().getClassLoader() ).getCacheManager();
  }

}
