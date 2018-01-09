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
