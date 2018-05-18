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
