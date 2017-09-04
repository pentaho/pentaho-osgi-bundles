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
package org.pentaho.webpackage.internal.osgi;

import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.PentahoWebPackageService;
import org.pentaho.webpackage.internal.PentahoWebPackageServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
  private PentahoWebPackageServiceImpl pentahoWebPackageService;
  private ServiceRegistration<PentahoWebPackageService> serviceReference;

  public void start( BundleContext bundleContext ) {
    this.pentahoWebPackageService = new PentahoWebPackageServiceImpl();

    bundleContext.addBundleListener( this.pentahoWebPackageService );

    this.serviceReference = bundleContext.registerService( PentahoWebPackageService.class, this.pentahoWebPackageService, null );
  }

  public void stop( BundleContext bundleContext ) {
    if ( this.pentahoWebPackageService != null ) {
      bundleContext.removeBundleListener( this.pentahoWebPackageService );

      this.serviceReference.unregister();

      this.pentahoWebPackageService = null;
    }
  }
}
