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

import org.pentaho.caching.spi.AbstractCacheProvidingService;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.ri.impl.GuavaCacheManager;

import javax.cache.CacheManager;

/**
 * @author nhudak
 */
public class HeapCacheProvidingService extends AbstractCacheProvidingService {
  @Override public CacheManager createCacheManager( PentahoCacheSystemConfiguration systemConfiguration ) {
    return new GuavaCacheManager();
  }
}
