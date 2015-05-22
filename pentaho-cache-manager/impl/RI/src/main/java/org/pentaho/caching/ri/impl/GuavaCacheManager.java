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

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * @author nhudak
 */
public class GuavaCacheManager implements CacheManager {
  private final Map<String, WrappedCache<?, ?>> managedCacheMap;
  private volatile boolean closed = false;

  public GuavaCacheManager() {
    managedCacheMap = Collections.synchronizedMap( Maps.<String, WrappedCache<?, ?>>newHashMap() );
  }

  @Override public CachingProvider getCachingProvider() {
    return null;
  }

  @Override public URI getURI() {
    return URI.create( GuavaCacheManager.class.getName() );
  }

  @Override public ClassLoader getClassLoader() {
    return GuavaCacheManager.class.getClassLoader();
  }

  @Override public Properties getProperties() {
    return new Properties();
  }

  @Override
  public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache( final String cacheName, final C configuration )
    throws IllegalArgumentException {
    synchronized ( managedCacheMap ) {
      Preconditions.checkArgument( !managedCacheMap.containsKey( cacheName ), "Cache %s already exists", cacheName );

      CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

      WrappedCache<K, V> newCache = new WrappedCache<K, V>( cacheBuilder.<K, V>build() ) {
        @Override public String getName() {
          return cacheName;
        }

        @Override public CacheManager getCacheManager() {
          return GuavaCacheManager.this;
        }

        @Override public void close() {
          synchronized ( managedCacheMap ) {
            if ( managedCacheMap.get( cacheName ) == this ) {
              managedCacheMap.remove( cacheName );
            }
          }
          super.close();
        }

        @Override public <T extends Configuration<K, V>> T getConfiguration( Class<T> clazz ) {
          return Constants.unwrap( configuration, clazz );
        }
      };

      managedCacheMap.put( cacheName, newCache );
      return newCache;
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override public Cache<?, ?> getCache( String cacheName ) {
    return getCache( cacheName, Object.class, Object.class );
  }

  @SuppressWarnings( "unchecked" )
  @Override public <K, V> Cache<K, V> getCache( String cacheName, Class<K> keyType, Class<V> valueType ) {
    final WrappedCache<?, ?> cache;
    Preconditions.checkNotNull( cacheName, "Cache Name can not be null" );
    Preconditions.checkNotNull( keyType, "Key Type can not be null" );
    Preconditions.checkNotNull( valueType, "Value Type can not be null" );

    synchronized ( managedCacheMap ) {
      if ( managedCacheMap.containsKey( cacheName ) ) {
        cache = managedCacheMap.get( cacheName );
      } else {
        MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>();
        configuration.setTypes( keyType, valueType );
        return createCache( cacheName, configuration );
      }
    }

    Configuration configuration = cache.getConfiguration( Configuration.class );
    Preconditions.checkArgument( keyType.equals( configuration.getKeyType() ), "Key Type is incompatible" );
    Preconditions.checkArgument( valueType.equals( configuration.getValueType() ), "Key Type is incompatible" );
    return (Cache<K, V>) cache;
  }

  @Override public Iterable<String> getCacheNames() {
    return managedCacheMap.keySet();
  }

  @Override public void destroyCache( String cacheName ) {
    WrappedCache<?, ?> cache = managedCacheMap.get( cacheName );
    if ( cache != null ) {
      cache.close();
    }
  }

  @Override public void enableManagement( String cacheName, boolean enabled ) {
  }

  @Override public void enableStatistics( String cacheName, boolean enabled ) {
  }

  @Override public void close() {
    for ( WrappedCache<?, ?> cache : managedCacheMap.values() ) {
      cache.close();
    }
    closed = true;
  }

  @Override public boolean isClosed() {
    return closed;
  }

  @Override public <T> T unwrap( Class<T> clazz ) {
    return Constants.unwrap( this, clazz );
  }
}
