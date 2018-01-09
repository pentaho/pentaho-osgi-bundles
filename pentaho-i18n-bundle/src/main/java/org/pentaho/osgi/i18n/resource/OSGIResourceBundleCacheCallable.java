/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.i18n.resource;

import org.pentaho.osgi.i18n.settings.OSGIResourceNameComparator;
import org.pentaho.osgi.i18n.settings.OSGIResourceNamingConvention;

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

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleCacheCallable implements Callable<Map<String, OSGIResourceBundle>> {
  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap;

  public OSGIResourceBundleCacheCallable( Map<Long, Map<String, OSGIResourceBundleFactory>> configMap ) {
    this.configMap = configMap;
  }

  @Override public Map<String, OSGIResourceBundle> call() throws Exception {
    Map<String, OSGIResourceBundleFactory> factoryMap =
      new TreeMap<>( new OSGIResourceNameComparator() );
    // Select only bundles with highest priority
    for ( Map<String, OSGIResourceBundleFactory> bundleMap : configMap.values() ) {
      for ( Map.Entry<String, OSGIResourceBundleFactory> entry : bundleMap.entrySet() ) {
        OSGIResourceBundleFactory pathToFactoryMap = entry.getValue();
        String key = entry.getValue().getPropertyFilePath();
        factoryMap.put( key, pathToFactoryMap );
      }
    }

    // Create bundles from factories
    Map<String, OSGIResourceBundle> result = new HashMap<String, OSGIResourceBundle>();
    Set<String> keys = factoryMap.keySet();
    SortedMap<String, OSGIResourceBundleFactory> factoryMapCopy =
      new TreeMap<>( new OSGIResourceNameComparator() );
    factoryMapCopy.putAll( factoryMap );
    for ( String key : keys ) {
      OSGIResourceBundleFactory nameToFactoryEntry = factoryMap.get( key );
      String name = nameToFactoryEntry.getPropertyFilePath();
      Matcher defaultMatcher = OSGIResourceNamingConvention.getResourceNameMatcher( name );
      String defaultName = defaultMatcher.group( 1 );
      String locale = defaultMatcher.group( 2 );
      if ( locale.length() > 1 ) {
        locale = locale.substring( 1 ).replace( '_', '-' );
      }
      OSGIResourceBundleFactory defaultFactory = null;
      List<String> candidates =
        OSGIResourceNamingConvention.getCandidateNames( defaultName, Locale.forLanguageTag( locale ) );
      // first try to get closest parent from properties with same locale and then in properties with "lower" locale
      for ( int i = 0; i < candidates.size(); i++ ) {
        Optional<String> firstKeyInHierarhy = getFirstKeyInHierarhy( factoryMapCopy, candidates.get( i )
          + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION );
        //checks that we've found not the same properties as original
        if ( firstKeyInHierarhy.isPresent()
          && factoryMapCopy.get( firstKeyInHierarhy.get() ) != nameToFactoryEntry ) {
          if ( i != candidates.size() - 1 ) {
            defaultFactory = factoryMapCopy.remove( firstKeyInHierarhy.get() );
          } else {
            defaultFactory = factoryMapCopy.get( firstKeyInHierarhy.get() );
          }
          break;
        }
      }
      OSGIResourceBundle parentBundle = null;
      if ( defaultFactory != null ) {
        parentBundle = result.get( defaultFactory.getBundle( null ).getDefaultName() );
      }
      OSGIResourceBundle resultKeyBundles = nameToFactoryEntry.getBundle( parentBundle );
      result.put( resultKeyBundles.getDefaultName(), resultKeyBundles );
    }
    return result;
  }

  /**
   * Gets first key from SortedMap with starts with parameter name
   *
   * @param factoryMapWithoutCurrent map to search from
   * @param name                     starting string
   * @return key
   */
  private Optional<String> getFirstKeyInHierarhy( SortedMap<String, OSGIResourceBundleFactory> factoryMapWithoutCurrent,
                                                  String name ) {
    return factoryMapWithoutCurrent.keySet().stream()
      .filter( new Predicate<String>() {
        @Override public boolean test( String s ) {
          return s.startsWith( name );
        }
      } )
      .findFirst();
  }
}
