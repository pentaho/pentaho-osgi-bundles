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

package org.pentaho.caching.ri.impl;

import com.google.common.cache.CacheBuilder;
import org.pentaho.caching.api.AbstractCacheManager;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;

/**
 * @author nhudak
 */
public class GuavaCacheManager extends AbstractCacheManager {

  @Override
  public <K, V, C extends Configuration<K, V>> Cache<K, V> newCache( final String cacheName, final C configuration ) {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

    return new WrappedCache<K, V>( cacheBuilder.<K, V>build() ) {
      @Override public String getName() {
        return cacheName;
      }

      @Override public CacheManager getCacheManager() {
        return GuavaCacheManager.this;
      }

      @Override public void close() {
        if ( !isClosed() ) {
          super.close();
          destroyCache( cacheName );
        }
      }

      @Override public <T extends Configuration<K, V>> T getConfiguration( Class<T> clazz ) {
        return Constants.unwrap( configuration, clazz );
      }
    };
  }
}
