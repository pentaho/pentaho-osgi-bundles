/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.caching.api;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.Map;

/**
 * @author nhudak
 */
public interface PentahoCacheManager extends CacheManager {
  PentahoCacheSystemConfiguration getSystemConfiguration();

  PentahoCacheProvidingService getService();

  <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                  Map<String, String> properties ) throws IllegalArgumentException;

  Map<String, PentahoCacheTemplateConfiguration> getTemplates();
}
