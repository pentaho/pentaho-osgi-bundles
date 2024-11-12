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
