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
