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

package org.pentaho.caching.spi;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import org.pentaho.caching.api.Constants.ExpiryFunction;
import org.pentaho.caching.api.PentahoCacheProvidingService;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;

import static org.pentaho.caching.api.Constants.CONFIG_STORE_BY_VALUE;
import static org.pentaho.caching.api.Constants.CONFIG_TTL;
import static org.pentaho.caching.api.Constants.CONFIG_TTL_RESET;
import static org.pentaho.caching.api.Constants.CONFIG_TTL_RESET_DEFAULT;

/**
 * @author nhudak
 */
public abstract class AbstractCacheProvidingService implements PentahoCacheProvidingService {
  @Override public <K, V> CompleteConfiguration<K, V> createConfiguration( Class<K> keyType, Class<V> valueType,
                                                                           Map<String, String> properties ) {
    MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>();
    configuration.setTypes( keyType, valueType );

    if ( properties.containsKey( CONFIG_TTL ) ) {
      Long ttl = Longs.tryParse( Strings.nullToEmpty( properties.get( CONFIG_TTL ) ) );
      Preconditions.checkArgument( ttl != null, "Template config error", CONFIG_TTL );

      Optional<ExpiryFunction> expiryFunction;
      if ( properties.containsKey( CONFIG_TTL_RESET ) ) {
        expiryFunction = Enums.getIfPresent( ExpiryFunction.class, properties.get( CONFIG_TTL_RESET ) );
      } else {
        expiryFunction = Optional.of( CONFIG_TTL_RESET_DEFAULT );
      }
      Preconditions.checkArgument( expiryFunction.isPresent(), "Template config error", CONFIG_TTL_RESET );

      configuration.setExpiryPolicyFactory( expiryFunction.get().createFactory( ttl ) );
    }
    if ( properties.containsKey( CONFIG_STORE_BY_VALUE ) ) {
      configuration.setStoreByValue( Boolean.valueOf( properties.get( CONFIG_STORE_BY_VALUE ) ) );
    }
    return configuration;
  }
}
