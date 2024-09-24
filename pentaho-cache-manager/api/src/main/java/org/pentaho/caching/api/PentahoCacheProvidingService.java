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
package org.pentaho.caching.api;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.Map;

/**
 * @author nhudak
 */
public interface PentahoCacheProvidingService {
  CacheManager createCacheManager( PentahoCacheSystemConfiguration systemConfiguration );

  <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                  Map<String, String> properties ) throws IllegalArgumentException;
}
