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

import static org.pentaho.caching.api.Constants.*;

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
