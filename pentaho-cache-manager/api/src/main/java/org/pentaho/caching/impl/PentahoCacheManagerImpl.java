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
