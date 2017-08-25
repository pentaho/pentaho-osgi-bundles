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
