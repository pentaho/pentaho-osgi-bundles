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
package org.pentaho.webpackage.extender.http.impl.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.webpackage.core.IPentahoWebPackage;

public class Activator implements BundleActivator {
  ServiceTracker pentahoWebPackageServiceTracker;

  public void start( BundleContext bundleContext ) {
    this.pentahoWebPackageServiceTracker = createPentahoWebPackageServiceTracker( bundleContext );

    this.pentahoWebPackageServiceTracker.open( true );
  }

  public void stop( BundleContext bundleContext ) {
    if ( this.pentahoWebPackageServiceTracker != null ) {
      this.pentahoWebPackageServiceTracker.close();

      this.pentahoWebPackageServiceTracker = null;
    }
  }

  // for unit test mocking
  ServiceTracker createPentahoWebPackageServiceTracker( BundleContext bundleContext ) {
    return new ServiceTracker<>( bundleContext, IPentahoWebPackage.class, new PentahoWebPackageServiceTracker( bundleContext ) );
  }
}
