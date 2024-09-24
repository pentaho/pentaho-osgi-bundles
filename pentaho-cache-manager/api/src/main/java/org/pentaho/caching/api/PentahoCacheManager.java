/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
