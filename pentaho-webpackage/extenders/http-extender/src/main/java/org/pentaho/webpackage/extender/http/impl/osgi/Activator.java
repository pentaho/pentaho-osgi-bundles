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
