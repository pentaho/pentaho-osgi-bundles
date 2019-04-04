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

import org.ops4j.pax.web.service.whiteboard.ResourceMapping;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.extender.http.impl.PentahoWebPackageResourceMapping;

/**
 * Tracks registered {@link IPentahoWebPackage} services and in turn registers {@link PentahoWebPackageResourceMapping} service.
 */
public class PentahoWebPackageServiceTracker implements ServiceTrackerCustomizer<IPentahoWebPackage, ServiceRegistration<?>> {
  private final BundleContext context;

  public PentahoWebPackageServiceTracker( BundleContext context ) {
    this.context = context;
  }

  @Override
  public ServiceRegistration<?> addingService( ServiceReference<IPentahoWebPackage> reference ) {
    Bundle bundle = reference.getBundle();
    // if null then the service is unregistered
    if ( bundle != null ) {
      ResourceMapping mapping = new PentahoWebPackageResourceMapping( this.context.getService( reference ) );

      return bundle.getBundleContext().registerService( ResourceMapping.class.getName(), mapping, null );
    }

    return null;
  }

  @Override
  public void modifiedService( ServiceReference<IPentahoWebPackage> reference, ServiceRegistration<?> serviceRegistration ) {
  }

  @Override
  public void removedService( ServiceReference<IPentahoWebPackage> reference, ServiceRegistration<?> serviceRegistration ) {
    this.context.ungetService( reference );

    try {
      serviceRegistration.unregister();
    } catch ( RuntimeException ignored ) {
      // service might be already unregistered automatically by the bundle lifecycle manager
    }
  }
}
