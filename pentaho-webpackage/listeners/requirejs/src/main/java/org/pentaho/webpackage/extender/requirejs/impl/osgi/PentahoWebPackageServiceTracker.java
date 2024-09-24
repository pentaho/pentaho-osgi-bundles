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
package org.pentaho.webpackage.extender.requirejs.impl.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.extender.requirejs.impl.RequireJsPackageImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Tracks registered {@link IPentahoWebPackage} services and in turn registers {@link IRequireJsPackage} service.
 */
public class PentahoWebPackageServiceTracker implements ServiceTrackerCustomizer<IPentahoWebPackage, ServiceRegistration<?>> {
  private final BundleContext context;

  PentahoWebPackageServiceTracker( BundleContext context ) {
    this.context = context;
  }

  @Override
  public ServiceRegistration<?> addingService( ServiceReference<IPentahoWebPackage> reference ) {
    Bundle bundle = reference.getBundle();
    // if null then the service is unregistered
    if ( bundle != null ) {
      IPentahoWebPackage pentahoWebPackage = this.context.getService( reference );

      URI resourceRootUri = null;
      try {
        URL resourceRootUrl = bundle.getResource( pentahoWebPackage.getResourceRootPath() );
        if ( resourceRootUrl != null ) {
          resourceRootUri = resourceRootUrl.toURI();
        }
      } catch ( URISyntaxException ignored ) {
      }

      IRequireJsPackage packageInfo = new RequireJsPackageImpl( pentahoWebPackage, resourceRootUri );
      return bundle.getBundleContext().registerService( IRequireJsPackage.class.getName(), packageInfo, null );
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
