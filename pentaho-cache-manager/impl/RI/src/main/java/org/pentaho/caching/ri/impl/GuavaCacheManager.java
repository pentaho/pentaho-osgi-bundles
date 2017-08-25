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

import com.google.common.cache.CacheBuilder;
import org.pentaho.caching.spi.AbstractCacheManager;
import org.pentaho.caching.api.Constants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

/**
 * @author nhudak
 */
public class GuavaCacheManager extends AbstractCacheManager {

  @Override
  public <K, V, C extends Configuration<K, V>> Cache<K, V> newCache( final String cacheName, final C configuration ) {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

    if ( configuration instanceof CompleteConfiguration ) {
      configureCacheBuilder( (CompleteConfiguration) configuration, cacheBuilder );
    }

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

  <K, V> void configureCacheBuilder( CompleteConfiguration<K, V> completeConfiguration,
                                     CacheBuilder<Object, Object> cacheBuilder ) {
    ExpiryPolicy expiryPolicy = completeConfiguration.getExpiryPolicyFactory().create();

    Duration expiryForAccess = expiryPolicy.getExpiryForAccess();
    if ( expiryForAccess != null && !expiryForAccess.isEternal() ) {
      cacheBuilder.expireAfterAccess( expiryForAccess.getDurationAmount(), expiryForAccess.getTimeUnit() );
    }
    Duration expiryForUpdate = expiryPolicy.getExpiryForUpdate();
    if ( expiryForUpdate != null && !expiryForUpdate.isEternal() ) {
      cacheBuilder.expireAfterWrite( expiryForUpdate.getDurationAmount(), expiryForUpdate.getTimeUnit() );
    }
  }
}
