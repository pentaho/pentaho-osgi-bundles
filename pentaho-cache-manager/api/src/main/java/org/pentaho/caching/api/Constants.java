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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import javax.cache.configuration.Factory;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.ModifiedExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author nhudak
 */
public class Constants {
  public static final String PENTAHO_CACHE_PROVIDER = "pentaho.cache.provider";

  public static final String DEFAULT_TEMPLATE = "default";
  public static final String DEFAULT_TEMPLATE_DESCRIPTION = "Default Cache Template";

  public static final String CONFIG_TTL = "ttl";
  public static final String CONFIG_TTL_RESET = "ttl.resetOn";
  public static final ExpiryFunction CONFIG_TTL_RESET_DEFAULT = ExpiryFunction.TOUCH;

  public static final String CONFIG_STORE_BY_VALUE = "storeByValue";

  public enum ExpiryFunction {
    CREATE {
      @Override public Factory<? extends ExpiryPolicy> createFactory( Long seconds ) {
        return CreatedExpiryPolicy.factoryOf( getDuration( seconds ) );
      }
    },
    MODIFY {
      @Override public Factory<? extends ExpiryPolicy> createFactory( Long seconds ) {
        return ModifiedExpiryPolicy.factoryOf( getDuration( seconds ) );
      }
    },
    ACCESS {
      @Override public Factory<? extends ExpiryPolicy> createFactory( Long seconds ) {
        return AccessedExpiryPolicy.factoryOf( getDuration( seconds ) );
      }
    },
    TOUCH {
      @Override public Factory<? extends ExpiryPolicy> createFactory( Long seconds ) {
        return TouchedExpiryPolicy.factoryOf( getDuration( seconds ) );
      }
    };

    private static Duration getDuration( Long seconds ) {
      return new Duration( TimeUnit.SECONDS, seconds );
    }

    public Factory<? extends ExpiryPolicy> createFactory( Long seconds ) {
      return EternalExpiryPolicy.factoryOf();
    }
  }

  public static Map<String, String> convertDictionary( Dictionary<String, ?> dictionary ) {
    Map<String, String> properties = Maps.newHashMapWithExpectedSize( dictionary.size() );
    for ( Enumeration<String> keys = dictionary.keys(); keys.hasMoreElements(); ) {
      String key = keys.nextElement();
      properties.put( key, String.valueOf( dictionary.get( key ) ) );
    }
    return properties;
  }

  public static <T> T unwrap( Object object, Class<T> clazz ) {
    Preconditions.checkArgument( clazz.isInstance( object ), "%s cannot be cast to %s", object, clazz );
    return clazz.cast( object );
  }
}
