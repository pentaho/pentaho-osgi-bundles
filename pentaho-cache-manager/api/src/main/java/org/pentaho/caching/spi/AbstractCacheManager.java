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
package org.pentaho.caching.spi;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author nhudak
 */
@SuppressWarnings( "unchecked" )
public abstract class AbstractCacheManager implements CacheManager {
  private final ConcurrentMap<String, Cache> managedCacheMap;
  private final AtomicBoolean closed = new AtomicBoolean( false );

  public AbstractCacheManager() {
    managedCacheMap = Maps.newConcurrentMap();
  }

  @Override public CachingProvider getCachingProvider() {
    return null;
  }

  @Override public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache( String cacheName, C configuration ) {
    Preconditions.checkState( !closed.get(), "Cache manager is closed" );
    Cache<K, V> cache = null;
    try {
      cache = newCache( cacheName, configuration );
      if ( managedCacheMap.putIfAbsent( cacheName, cache ) == null ) {
        return cache;
      } else {
        throw new CacheException( "Cache already exists: " + cacheName );
      }
    } catch ( Throwable t ) {
      if ( cache != null ) {
        cache.close();
      }
      throw Throwables.propagate( t );
    }
  }

  protected abstract <K, V, C extends Configuration<K, V>> Cache<K, V> newCache( String cacheName, C configuration );

  @Override public URI getURI() {
    return URI.create( getClass().getName() );
  }

  @Override public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Override public Properties getProperties() {
    return new Properties();
  }

  @Override public <K, V> Cache<K, V> getCache( String cacheName ) {
    return (Cache<K, V>) getCache( cacheName, Object.class, Object.class );
  }

  @Override public <K, V> Cache<K, V> getCache( String cacheName, Class<K> keyType, Class<V> valueType ) {
    Preconditions.checkState( !closed.get(), "Cache manager is closed" );
    final Cache<?, ?> cache;
    Preconditions.checkNotNull( cacheName, "Cache Name can not be null" );
    Preconditions.checkNotNull( keyType, "Key Type can not be null" );
    Preconditions.checkNotNull( valueType, "Value Type can not be null" );

    cache = managedCacheMap.get( cacheName );
    if ( cache == null ) {
      return null;
    }

    Configuration<K, V> configuration = cache.getConfiguration( Configuration.class );
    Preconditions.checkArgument( keyType.equals( configuration.getKeyType() ), "Key Type is incompatible" );
    Preconditions.checkArgument( valueType.equals( configuration.getValueType() ), "Value Type is incompatible" );
    return (Cache<K, V>) cache;
  }

  @Override public Iterable<String> getCacheNames() {
    return managedCacheMap.keySet();
  }

  @Override public void destroyCache( String cacheName ) {
    Cache<?, ?> cache = managedCacheMap.remove( cacheName );
    if ( cache != null ) {
      cache.close();
    }
  }

  @Override public void enableManagement( String cacheName, boolean enabled ) {
  }

  @Override public void enableStatistics( String cacheName, boolean enabled ) {
  }

  @Override public void close() {
    closed.set( true );
    for ( String cacheName : managedCacheMap.keySet() ) {
      destroyCache( cacheName );
    }
  }

  @Override public boolean isClosed() {
    return closed.get();
  }

  @Override public <T> T unwrap( Class<T> clazz ) {
    return Constants.unwrap( this, clazz );
  }
}
