/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.caching.impl;

import org.pentaho.caching.api.PentahoCacheManager;
import org.pentaho.caching.api.PentahoCacheProvidingService;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.api.PentahoCacheTemplateConfiguration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

/**
 * @author nhudak
 */
public class PentahoCacheManagerImpl implements PentahoCacheManager {
  private final PentahoCacheSystemConfiguration systemConfiguration;
  private final PentahoCacheProvidingService service;
  private final CacheManager cacheManager;

  public PentahoCacheManagerImpl( PentahoCacheSystemConfiguration systemConfiguration,
                                  PentahoCacheProvidingService service ) {
    this.systemConfiguration = systemConfiguration;
    this.service = service;
    cacheManager = service.createCacheManager( systemConfiguration );
  }

  private CacheManager delegate() {
    return cacheManager;
  }

  @Override public CachingProvider getCachingProvider() {
    return delegate().getCachingProvider();
  }

  @Override public URI getURI() {
    return delegate().getURI();
  }

  @Override public ClassLoader getClassLoader() {
    return delegate().getClassLoader();
  }

  @Override public Properties getProperties() {
    return delegate().getProperties();
  }

  @Override public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache( String cacheName, C configuration )
    throws IllegalArgumentException {
    return delegate().createCache( cacheName, configuration );
  }

  @Override public <K, V> Cache<K, V> getCache( String cacheName, Class<K> keyType, Class<V> valueType ) {
    return delegate().getCache( cacheName, keyType, valueType );
  }

  @Override public <K, V> Cache<K, V> getCache( String cacheName ) {
    return delegate().getCache( cacheName );
  }

  @Override public Iterable<String> getCacheNames() {
    return delegate().getCacheNames();
  }

  @Override public void destroyCache( String cacheName ) {
    delegate().destroyCache( cacheName );
  }

  @Override public void enableManagement( String cacheName, boolean enabled ) {
    delegate().enableManagement( cacheName, enabled );
  }

  @Override public void enableStatistics( String cacheName, boolean enabled ) {
    delegate().enableStatistics( cacheName, enabled );
  }

  @Override public void close() {
    delegate().close();
  }

  @Override public boolean isClosed() {
    return delegate().isClosed();
  }

  @Override public <T> T unwrap( Class<T> clazz ) {
    if ( clazz.isInstance( this ) ) {
      return clazz.cast( this );
    } else {
      return delegate().unwrap( clazz );
    }
  }

  @Override public PentahoCacheSystemConfiguration getSystemConfiguration() {
    return systemConfiguration;
  }

  @Override public PentahoCacheProvidingService getService() {
    return service;
  }

  @Override public <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                                   Map<String, String> properties ) {
    return service.createConfiguration( keyType, valueType, properties );
  }

  @Override public Map<String, PentahoCacheTemplateConfiguration> getTemplates() {
    return systemConfiguration.createTemplates( this );
  }
}
