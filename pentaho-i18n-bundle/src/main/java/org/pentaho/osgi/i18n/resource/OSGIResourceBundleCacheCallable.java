/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.pentaho.osgi.i18n.settings.OSGIResourceNamingConvention;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
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
      Matcher defaultMatcher = OSGIResourceNamingConvention.getResourceNameMatcher( name );
      String defaultName = defaultMatcher.group( 1 );
      OSGIResourceBundleFactory defaultFactory =
        factoryMap.get( defaultName + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION );
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
