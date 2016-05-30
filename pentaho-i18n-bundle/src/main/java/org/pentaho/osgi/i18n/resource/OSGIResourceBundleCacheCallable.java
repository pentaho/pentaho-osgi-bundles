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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleCacheCallable implements Callable<Map<String, OSGIResourceBundle>> {
  public static final Pattern DEFAULT_PATTERN = Pattern.compile( "(.*/[^_]+)(.*).properties(\\.\\d+)?" );
  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap;

  public OSGIResourceBundleCacheCallable( Map<Long, Map<String, OSGIResourceBundleFactory>> configMap ) {
    this.configMap = configMap;
  }

  public static Matcher getDefault( String path ) {
    Matcher matcher = DEFAULT_PATTERN.matcher( path );
    boolean matches = matcher.matches();
    if ( matches ) {
      return matcher;
    } else {
      throw new IllegalArgumentException(
        "Path must be of the form prefix/filename[_internationalization].properties[.priority]" );
    }
  }

  @Override public Map<String, OSGIResourceBundle> call() throws Exception {
    Map<String, OSGIResourceBundleFactory> factoryMap =
      new HashMap<String, OSGIResourceBundleFactory>();
    // Select only bundles with highest priority
    for ( Map<String, OSGIResourceBundleFactory> bundleMap : configMap.values() ) {
      for ( Map.Entry<String, OSGIResourceBundleFactory> entry : bundleMap.entrySet() ) {
        String key = entry.getValue().getPropertyFilePath();
        OSGIResourceBundleFactory pathToFactoryMap = factoryMap.get( key );
        if ( pathToFactoryMap == null || pathToFactoryMap.getPriority() < entry.getValue().getPriority() ) {
          pathToFactoryMap = entry.getValue();
        }
        factoryMap.put( key, pathToFactoryMap );
      }
    }

    // Create bundles from factories
    Map<String, OSGIResourceBundle> result = new HashMap<String, OSGIResourceBundle>();
    for ( Map.Entry<String, OSGIResourceBundleFactory> factoryMapEntry : factoryMap.entrySet() ) {
      OSGIResourceBundle resultKeyBundles;
      OSGIResourceBundleFactory nameToFactoryEntry = factoryMapEntry.getValue();
      String name = nameToFactoryEntry.getPropertyFilePath();
      Matcher defaultMatcher = getDefault( name );
      String defaultName = defaultMatcher.group( 1 );
      OSGIResourceBundleFactory defaultFactory = factoryMap.get( defaultName + ".properties" );
      OSGIResourceBundle parentBundle = null;
      if ( defaultFactory != null && defaultFactory != nameToFactoryEntry ) {
        parentBundle = defaultFactory.getBundle( null );
      }
      resultKeyBundles = nameToFactoryEntry.getBundle( parentBundle );
      result.put( resultKeyBundles.getDefaultName(), resultKeyBundles );
    }
    return result;
  }
}
