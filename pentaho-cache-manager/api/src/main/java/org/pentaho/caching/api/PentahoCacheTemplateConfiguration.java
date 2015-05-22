/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import java.util.Map;

/**
 * @author nhudak
 */
public class PentahoCacheTemplateConfiguration {
  private final String description;
  private final ImmutableMap<String, String> properties;
  private final PentahoCacheManager cacheManager;

  public PentahoCacheTemplateConfiguration( String description, Map<String, String> properties,
                                            PentahoCacheManager cacheManager ) {
    this.description = description;
    this.properties = ImmutableMap.copyOf( properties );
    this.cacheManager = cacheManager;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public PentahoCacheManager getCacheManager() {
    return cacheManager;
  }

  public <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType ) {
    return cacheManager.createConfiguration( keyType, valueType, properties );
  }

  public <K, V> Cache<K, V> createCache( String cacheName, Class<K> keyType, Class<V> valueType )
    throws IllegalArgumentException {
    return cacheManager.createCache( cacheName, createConfiguration( keyType, valueType ) );
  }
}
