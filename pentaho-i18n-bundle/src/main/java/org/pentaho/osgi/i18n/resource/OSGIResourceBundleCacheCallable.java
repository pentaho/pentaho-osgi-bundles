/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleCacheCallable implements Callable<Map<String, Map<String, OSGIResourceBundle>>> {
  private static final Pattern DEFAULT_PATTERN = Pattern.compile( "(.*/[^_]+)(.*).properties" );
  private final Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap;

  public OSGIResourceBundleCacheCallable( Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap ) {
    this.configMap = configMap;
  }

  private static Matcher getDefault( String path ) {
    Matcher matcher = DEFAULT_PATTERN.matcher( path );
    boolean matches = matcher.matches();
    if ( matches ) {
      return matcher;
    } else {
      throw new IllegalArgumentException(
        "Path must be of the form prefix/filename[_internationalization].properties" );
    }
  }

  @Override public Map<String, Map<String, OSGIResourceBundle>> call() throws Exception {
    Map<String, Map<String, OSGIResourceBundleFactory>> factoryMap =
      new HashMap<String, Map<String, OSGIResourceBundleFactory>>();
    // Select only bundles with highest priority
    for ( Map<String, List<OSGIResourceBundleFactory>> bundleMap : configMap.values() ) {
      for ( Map.Entry<String, List<OSGIResourceBundleFactory>> entry : bundleMap.entrySet() ) {
        String key = entry.getKey();
        for ( OSGIResourceBundleFactory bundleFactory : entry.getValue() ) {
          Map<String, OSGIResourceBundleFactory> pathToFactoryMap = factoryMap.get( key );
          if ( pathToFactoryMap == null ) {
            pathToFactoryMap = new HashMap<String, OSGIResourceBundleFactory>();
            factoryMap.put( key, pathToFactoryMap );
          }
          OSGIResourceBundleFactory existingFactory = pathToFactoryMap.get( bundleFactory.getPropertyFilePath() );
          if ( existingFactory == null || existingFactory.getPriority() < bundleFactory.getPriority() ) {
            pathToFactoryMap.put( bundleFactory.getPropertyFilePath(), bundleFactory );
          }
        }
      }
    }

    // Create bundles from factories
    Map<String, Map<String, OSGIResourceBundle>> result = new HashMap<String, Map<String, OSGIResourceBundle>>();
    for ( Map.Entry<String, Map<String, OSGIResourceBundleFactory>> factoryMapEntry : factoryMap.entrySet() ) {
      String key = factoryMapEntry.getKey();
      Map<String, OSGIResourceBundle> resultKeyBundles = new HashMap<String, OSGIResourceBundle>();
      result.put( key, resultKeyBundles );
      for ( Map.Entry<String, OSGIResourceBundleFactory> nameToFactoryEntry : factoryMapEntry.getValue().entrySet() ) {
        String name = nameToFactoryEntry.getKey();
        Matcher defaultMatcher = getDefault( name );
        String defaultName = defaultMatcher.group( 1 );
        name = defaultName + defaultMatcher.group( 2 );
        OSGIResourceBundleFactory defaultFactory = factoryMapEntry.getValue().get( defaultName + ".properties" );
        OSGIResourceBundle parentBundle = null;
        if ( defaultFactory != null && defaultFactory != nameToFactoryEntry.getValue() ) {
          parentBundle = defaultFactory.getBundle( null );
        }
        resultKeyBundles.put( name, nameToFactoryEntry.getValue().getBundle( parentBundle ) );
      }
    }
    return result;
  }
}
