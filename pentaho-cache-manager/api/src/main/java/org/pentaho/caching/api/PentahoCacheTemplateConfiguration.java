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
