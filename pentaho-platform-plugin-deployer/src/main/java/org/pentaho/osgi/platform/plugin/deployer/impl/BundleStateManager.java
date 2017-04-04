/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
/**
 * Manager holds the map of installed bundles.Provides a method to query if the
 * bundle name exist in the map.
 */
public class BundleStateManager {
  private static Logger log = LoggerFactory.getLogger( BundleStateManager.class );
  Map<String, Integer> bundleMap;


  public BundleStateManager() {
    bundleMap = new HashMap<String, Integer>();
  }

  public void setState( String name, int state ) {
    log.debug( "BundleStateManager adding the state for bundle: {}, with state: {}", name, state );
    bundleMap.put( name, state );
  }

  /**
   * Returns true is the bundle is the name is present in the map
   *  otherwise false
   */
  public boolean isBundleInstalled( String name ) {
    if ( bundleMap.containsKey( name ) ) {
      if ( bundleMap.get( name ) == BundleEvent.INSTALLED ) {
        return true;
      }
    }
    return false;
  }

}
