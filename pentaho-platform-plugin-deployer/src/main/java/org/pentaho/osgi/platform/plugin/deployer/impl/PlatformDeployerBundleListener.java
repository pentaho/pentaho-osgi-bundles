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

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a bundle listener that gets the callback whenever the bundle state changes.
 * It stores the state of the only those bundle that have the manifest entry in header
 * "Bundle-PlatformPluginName".
 */

public class PlatformDeployerBundleListener implements BundleListener {
  private static Logger log = LoggerFactory.getLogger( PlatformDeployerBundleListener.class );
  private BundleContext bundleContext;
  private BundleStateManager bundleStateManager;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void setBundleStateManager( BundleStateManager bundleStateManager ) {
    this.bundleStateManager = bundleStateManager;
  }

  // For unit test only
  static void setLog( Logger log ) {
    PlatformDeployerBundleListener.log = log;
  }

  // For unit test only
  static Logger getLog() {
    return log;
  }

  @Override public void bundleChanged( BundleEvent event ) {
    switch ( event.getType() ) {
      case BundleEvent.UNINSTALLED:
      case BundleEvent.INSTALLED:
        //Check if the bundle is platformPluginBundle
        if ( event.getBundle().getHeaders().get( "Bundle-PlatformPluginName" ) != null ) {
          String bundleStr = event.getBundle().getHeaders().get( "Bundle-Name" ) +  event.getBundle().getHeaders().get( "Bundle-Version" );
          log.info( "Received Bundle event : {}", bundleStr
                + ( ( event.getType() == BundleEvent.INSTALLED ) ? "Installed" : "Uninstalled" ) );
          bundleStateManager.setState( bundleStr, event.getType() );
        }
        break;
    }
  }

  public void init() throws Exception {
    bundleContext.addBundleListener( this );
  }
}
