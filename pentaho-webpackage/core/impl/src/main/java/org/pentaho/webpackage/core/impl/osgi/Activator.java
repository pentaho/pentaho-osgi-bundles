/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.webpackage.core.impl.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
  private PentahoWebPackageBundleListener pentahoWebPackageBundleListener;

  public void start( BundleContext bundleContext ) {
    this.pentahoWebPackageBundleListener = createPentahoWebPackageService();

    bundleContext.addBundleListener( this.pentahoWebPackageBundleListener );

    // add already stated bundles
    addActiveBundles( bundleContext.getBundles() );
  }

  private void addActiveBundles( Bundle[] bundles ) {
    if ( bundles != null ) {
      for ( Bundle bundle : bundles ) {
        if ( bundle.getState() == Bundle.ACTIVE ) {
          this.pentahoWebPackageBundleListener.registerWebPackageServices( bundle );
        }
      }
    }
  }

  public void stop( BundleContext bundleContext ) {
    if ( this.pentahoWebPackageBundleListener != null ) {
      bundleContext.removeBundleListener( this.pentahoWebPackageBundleListener );

      this.pentahoWebPackageBundleListener = null;
    }
  }

  // for unit test mocking
  PentahoWebPackageBundleListener createPentahoWebPackageService() {
    return new PentahoWebPackageBundleListener();
  }
}
