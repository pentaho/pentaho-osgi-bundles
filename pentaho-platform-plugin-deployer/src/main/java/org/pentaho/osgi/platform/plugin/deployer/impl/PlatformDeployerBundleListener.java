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
