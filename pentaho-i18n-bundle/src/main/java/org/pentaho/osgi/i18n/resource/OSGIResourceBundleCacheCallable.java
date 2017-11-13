/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.i18n.resource;

import org.pentaho.osgi.i18n.settings.OSGIResourceNameComparator;
import org.pentaho.osgi.i18n.settings.OSGIResourceNamingConvention;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class OSGIResourceBundleCacheCallable implements Callable<Map<String, OSGIResourceBundle>> {
  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap;

  public OSGIResourceBundleCacheCallable( Map<Long, Map<String, OSGIResourceBundleFactory>> configMap ) {
    this.configMap = configMap;
  }

  @Override
  public Map<String, OSGIResourceBundle> call() throws Exception {
    Map<String, OSGIResourceBundleFactory> factoryMap = new TreeMap<>( new OSGIResourceNameComparator() );

    // Select only bundles with highest priority
    for ( Map<String, OSGIResourceBundleFactory> bundleMap : this.configMap.values() ) {
      for ( Map.Entry<String, OSGIResourceBundleFactory> entry : bundleMap.entrySet() ) {
        OSGIResourceBundleFactory pathToFactoryMap = entry.getValue();
        String propertyFilePath = entry.getValue().getPropertyFilePath();
        factoryMap.put( propertyFilePath, pathToFactoryMap );
      }
    }

    // Create bundles from factories
    Map<String, OSGIResourceBundle> result = new HashMap<>();
    Set<String> propertyPaths = factoryMap.keySet();

    SortedMap<String, OSGIResourceBundleFactory> factoryMapCopy = new TreeMap<>( new OSGIResourceNameComparator() );
    factoryMapCopy.putAll( factoryMap );

    for ( String propertyFilepath : propertyPaths ) {
      OSGIResourceBundleFactory resourceBundleFactory = factoryMap.get( propertyFilepath );

      String resourceKey = resourceBundleFactory.getResourceKey();
      Locale resourceLocale = getLocale( propertyFilepath );

      List<String> candidates = OSGIResourceNamingConvention.getCandidateNames( resourceKey, resourceLocale );
      Collections.reverse( candidates );
      OSGIResourceBundleFactory defaultResourceFactory = getDefaultResourceFactory( candidates, factoryMapCopy, resourceBundleFactory );

      OSGIResourceBundle parentResourceBundle = null;
      if ( defaultResourceFactory != null ) {
        String defaultResourceName = defaultResourceFactory.getResourceBundle( null ).getDefaultName();
        parentResourceBundle = result.get( defaultResourceName );
      }

      OSGIResourceBundle resultResourceBundle = resourceBundleFactory.getResourceBundle( parentResourceBundle );
      result.put( resultResourceBundle.getDefaultName(), resultResourceBundle );
    }

    return result;
  }

  /**
   * Gets first key from SortedMap with starts with parameter name
   *
   * @param factoryMapWithoutCurrent map to search from
   * @param name                     starting string
   *
   * @return key
   */
  private Optional<String> getFirstKeyInHierarchy( SortedMap<String, OSGIResourceBundleFactory> factoryMapWithoutCurrent,
                                                   String resourceName ) {
    return factoryMapWithoutCurrent.keySet().stream()
      .filter( new Predicate<String>() {
        @Override
        public boolean test( String s ) {
          return s.startsWith( resourceName );
        }
      } )
      .findFirst();
  }

  private Locale getLocale( String propertyFilePath ) {
    Matcher defaultMatcher = OSGIResourceNamingConvention.getResourceNameMatcher( propertyFilePath );
    String locale = defaultMatcher.group( 2 );
    if ( locale.length() > 1 ) {
      locale = locale.substring( 1 ).replace( '_', '-' );
    }

    return Locale.forLanguageTag( locale );
  }

  private OSGIResourceBundleFactory getDefaultResourceFactory( List<String> candidates,
                                                               SortedMap<String, OSGIResourceBundleFactory> factoryMapCopy,
                                                               OSGIResourceBundleFactory resourceBundleFactory ) {
    OSGIResourceBundleFactory defaultFactory = null;

    // first try to get closest parent from properties with same locale and then in properties with "lower" locale
    for ( int i = 0; i < candidates.size(); i++ ) {
      Optional<String> firstKeyInHierarchy = getFirstKeyInHierarchy( factoryMapCopy, candidates.get( i )
          + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION );

      //checks that we've found not the same properties as original
      if ( firstKeyInHierarchy.isPresent()
          && factoryMapCopy.get( firstKeyInHierarchy.get() ) != resourceBundleFactory ) {
        if ( i != candidates.size() - 1 ) {
          defaultFactory = factoryMapCopy.remove( firstKeyInHierarchy.get() );
        } else {
          defaultFactory = factoryMapCopy.get( firstKeyInHierarchy.get() );
        }
        break;
      }
    }

    return defaultFactory;
  }
}
