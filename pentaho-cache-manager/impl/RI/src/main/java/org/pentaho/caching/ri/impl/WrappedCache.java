/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.caching.ri.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author nhudak
 */
abstract class WrappedCache<K, V> implements Cache<K, V> {
  private final com.google.common.cache.Cache<K, V> cache;
  private volatile boolean closed = false;

  public WrappedCache( com.google.common.cache.Cache<K, V> guavaCache ) {
    cache = guavaCache;
  }

  protected Optional<V> tryGet( K key ) {
    assertNotClosed();
    return Optional.fromNullable( cache.getIfPresent( key ) );
  }

  protected void assertNotClosed() throws CacheException{
    Preconditions.checkState( !isClosed(), "Cache is closed" );
  }

  @Override public V get( K key ) {
    assertNotClosed();
    return tryGet( key ).orNull();
  }

  @Override public Map<K, V> getAll( Set<? extends K> keys ) {
    assertNotClosed();
    return cache.getAllPresent( keys );
  }

  @Override public boolean containsKey( K key ) {
    assertNotClosed();
    return tryGet( key ).isPresent();
  }

  @Override
  public void loadAll( Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener ) {
    assertNotClosed();
    // Not supported
  }

  @Override public void put( K key, V value ) {
    assertNotClosed();
    cache.put( key, value );
  }

  @Override public V getAndPut( K key, V value ) {
    assertNotClosed();
    return cache.asMap().replace( key, value );
  }

  @Override public void putAll( Map<? extends K, ? extends V> map ) {
    assertNotClosed();
    cache.putAll( map );
  }

  @Override public boolean putIfAbsent( K key, V value ) {
    assertNotClosed();
    return cache.asMap().putIfAbsent( key, value ) == null;
  }

  @Override public boolean remove( K key ) {
    assertNotClosed();
    return cache.asMap().remove( key ) != null;
  }

  @Override public boolean remove( K key, V oldValue ) {
    assertNotClosed();
    return cache.asMap().remove( key, oldValue );
  }

  @Override public V getAndRemove( K key ) {
    assertNotClosed();
    return cache.asMap().remove( key );
  }

  @Override public boolean replace( K key, V oldValue, V newValue ) {
    assertNotClosed();
    return cache.asMap().replace( key, oldValue, newValue );
  }

  @Override public boolean replace( K key, V value ) {
    assertNotClosed();
    return cache.asMap().replace( key, value ) != null;
  }

  @Override public V getAndReplace( K key, V value ) {
    assertNotClosed();
    return cache.asMap().replace( key, value );
  }

  @Override public void removeAll( Set<? extends K> keys ) {
    assertNotClosed();
    cache.invalidateAll( keys );
  }

  @Override public void removeAll() {
    assertNotClosed();
    cache.asMap().clear();
  }

  @Override public void clear() {
    assertNotClosed();
    cache.asMap().clear();
  }

  @Override public <T> T invoke( K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments )
    throws EntryProcessorException {
    assertNotClosed();
    return invokeAll( ImmutableSet.of( key ), entryProcessor, arguments ).get( key ).get();
  }

  @Override
  public <T> Map<K, EntryProcessorResult<T>> invokeAll( final Set<? extends K> keys,
                                                        final EntryProcessor<K, V, T> entryProcessor,
                                                        final Object... arguments ) {
    assertNotClosed();
    ImmutableMap.Builder<K, EntryProcessorResult<T>> resultMap = ImmutableMap.builder();
    for ( final K key : keys ) {
      final AtomicBoolean removed = new AtomicBoolean( false ), updated = new AtomicBoolean( false );

      final MutableEntry<K, V> entry = new MutableEntry<K, V>() {
        Optional<V> value = tryGet( key );

        @Override public K getKey() {
          return key;
        }

        @Override public V getValue() {
          return value.orNull();
        }

        @Override public <U> U unwrap( Class<U> clazz ) {
          return Constants.unwrap( this, clazz );
        }

        @Override public boolean exists() {
          return value.isPresent();
        }

        @Override public void remove() {
          removed.set( true );
        }

        @Override public void setValue( V value ) {
          this.value = Optional.of( value );
          updated.set( true );
        }
      };

      final T result = entryProcessor.process( entry, arguments );

      if ( removed.get() ) {
        remove( key );
      } else if ( updated.get() ) {
        put( key, entry.getValue() );
      }

      resultMap.put( key, new EntryProcessorResult<T>() {
        @Override public T get() throws EntryProcessorException {
          return result;
        }
      } );
    }
    return resultMap.build();
  }

  @Override public Iterator<Entry<K, V>> iterator() {
    assertNotClosed();
    return FluentIterable.from( cache.asMap().entrySet() ).transform( new Function<Map.Entry<K, V>, Entry<K, V>>() {
      @Override public Entry<K, V> apply( final Map.Entry<K, V> mapEntry ) {
        return new Entry<K, V>() {
          @Override public K getKey() {
            return mapEntry.getKey();
          }

          @Override public V getValue() {
            return mapEntry.getValue();
          }

          @Override public <T> T unwrap( Class<T> clazz ) {
            return Constants.unwrap( this, clazz );
          }
        };
      }
    } ).iterator();
  }

  @Override
  public void registerCacheEntryListener( CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration ) {
    assertNotClosed();
    throw new CacheException( "CacheEntryListeners are not yet supported" );
  }

  @Override
  public void deregisterCacheEntryListener( CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration ) {
    assertNotClosed();
    throw new CacheException( "CacheEntryListeners are not yet supported" );
  }

  @Override public void close() {
    closed = true;
  }

  @Override public boolean isClosed() {
    return closed;
  }

  @Override public <T> T unwrap( Class<T> clazz ) {
    return Constants.unwrap( this, clazz );
  }

}
