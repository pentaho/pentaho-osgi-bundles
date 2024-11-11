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

package org.pentaho.caching.api;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import java.util.Map;

/**
 * @author nhudak
 */
public class PentahoCacheTemplateConfiguration {
  private final String description;
  private final ImmutableMap<String, String> properties;
  private final PentahoCacheManager cacheManager;

  public PentahoCacheTemplateConfiguration( String description, Map<String, String> properties,
                                            PentahoCacheManager cacheManager ) {
    this.description = description;
    this.properties = ImmutableMap.copyOf( properties );
    this.cacheManager = cacheManager;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public PentahoCacheManager getCacheManager() {
    return cacheManager;
  }

  public <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType )
    throws IllegalArgumentException {
    return cacheManager.createConfiguration( keyType, valueType, properties );
  }

  public <K, V> Cache<K, V> createCache( String cacheName, Class<K> keyType, Class<V> valueType )
    throws IllegalArgumentException {
    return cacheManager.createCache( cacheName, createConfiguration( keyType, valueType ) );
  }

  /**
   * Generates a new PentahoCacheTemplateConfiguration which merges the properties in the current
   * Configuration with those in templateOverrides, replacing existing entries if present.
   */
  public PentahoCacheTemplateConfiguration overrideProperties( final Map<String, String> templateOverrides ) {
    Map<String, String> overriddenProperties =
        ImmutableMap.<String, String>builder()
            .putAll( Maps.filterKeys( getProperties(), new Predicate<String>() {
              @Override public boolean apply( String s ) {
                return !templateOverrides.containsKey( s );
              }
            } ) ).putAll( templateOverrides ).build();
    return new PentahoCacheTemplateConfiguration( getDescription(), overriddenProperties, getCacheManager() );
  }
}
