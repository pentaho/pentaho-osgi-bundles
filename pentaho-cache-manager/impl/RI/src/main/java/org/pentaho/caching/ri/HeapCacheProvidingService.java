package org.pentaho.caching.ri;

import org.pentaho.caching.api.PentahoCacheProvidingService;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.ri.impl.GuavaCacheManager;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;

/**
 * @author nhudak
 */
public class HeapCacheProvidingService implements PentahoCacheProvidingService {
  @Override public CacheManager createCacheManager( PentahoCacheSystemConfiguration systemConfiguration ) {
    return new GuavaCacheManager();
  }

  @Override public <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                                   Map<String, String> properties ) {
    MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>();
    configuration.setTypes( keyType, valueType );
    return configuration;
  }
}
