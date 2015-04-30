package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;

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

  public <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType ) {
    return cacheManager.createConfiguration( keyType, valueType, properties );
  }

  public <K, V> Cache<K, V> createCache( String cacheName, Class<K> keyType, Class<V> valueType )
    throws IllegalArgumentException {
    return cacheManager.createCache( cacheName, createConfiguration( keyType, valueType ) );
  }
}
