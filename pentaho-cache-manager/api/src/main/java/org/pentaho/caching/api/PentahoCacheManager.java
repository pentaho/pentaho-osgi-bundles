package org.pentaho.caching.api;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.Map;

/**
 * @author nhudak
 */
public interface PentahoCacheManager extends CacheManager {
  PentahoCacheSystemConfiguration getSystemConfiguration();

  PentahoCacheProvidingService getService();

  <K, V> Configuration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                  Map<String, String> properties );

  Map<String, PentahoCacheTemplateConfiguration> getTemplates();
}
