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

package org.pentaho.caching.spi;

import org.pentaho.caching.api.PentahoCacheProvidingService;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;

/**
 * @author nhudak
 */
public abstract class AbstractCacheProvidingService implements PentahoCacheProvidingService {
  @Override public <K, V> CompleteConfiguration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                                           Map<String, String> properties ) {
    MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>();
    configuration.setTypes( keyType, valueType );

    return configuration;
  }
}
