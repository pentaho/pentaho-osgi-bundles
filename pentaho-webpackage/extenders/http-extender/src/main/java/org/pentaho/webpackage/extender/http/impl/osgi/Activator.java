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
 * Copyright 2017 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.webpackage.extender.http.impl.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.PentahoWebPackageService;
import org.pentaho.webpackage.extender.http.impl.PentahoWebPackageServiceImpl;

public class Activator implements BundleActivator {
  private PentahoWebPackageServiceImpl pentahoWebPackageService;
  private ServiceRegistration<PentahoWebPackageService> serviceReference;

  public void start( BundleContext bundleContext ) {
    this.pentahoWebPackageService = createPentahoWebPackageService();

    bundleContext.addBundleListener( this.pentahoWebPackageService );

    // add already stated bundles
    addActiveBundles( bundleContext.getBundles() );

    this.serviceReference = bundleContext.registerService( PentahoWebPackageService.class, this.pentahoWebPackageService, null );
  }

  void addActiveBundles( Bundle[] bundles ) {
    if ( bundles != null ) {
      for ( Bundle bundle : bundles ) {
        if ( bundle.getState() == Bundle.ACTIVE ) {
          this.pentahoWebPackageService.addBundle( bundle );
        }
      }
    }
  }

  public void stop( BundleContext bundleContext ) {
    if ( this.pentahoWebPackageService != null ) {
      bundleContext.removeBundleListener( this.pentahoWebPackageService );

      try {
        this.serviceReference.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.pentahoWebPackageService = null;
    }
  }

  // for unit test mocking
  PentahoWebPackageServiceImpl createPentahoWebPackageService() {
    return new PentahoWebPackageServiceImpl();
  }
}
