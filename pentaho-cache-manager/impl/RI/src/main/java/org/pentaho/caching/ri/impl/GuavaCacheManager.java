/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
