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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.extender.http.impl.PentahoWebPackageResourceMapping;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Tracks registered {@link IPentahoWebPackage} services and in turn registers {@link PentahoWebPackageResourceMapping} service.
 */
public class PentahoWebPackageServiceTracker implements ServiceTrackerCustomizer<IPentahoWebPackage, ServiceRegistration<?>> {
  private final BundleContext context;

  private static final String RESOURCE_PATTERN_KEY = "osgi.http.whiteboard.resource.pattern";
  private static final String RESOURCE_PREFIX_KEY = "osgi.http.whiteboard.resource.prefix";

  public PentahoWebPackageServiceTracker( BundleContext context ) {
    this.context = context;
  }

  @Override
  public ServiceRegistration<?> addingService( ServiceReference<IPentahoWebPackage> reference ) {
    Bundle bundle = reference.getBundle();

    // if null then the service is unregistered
    if ( bundle != null ) {
      PentahoWebPackageResourceMapping mapping = new PentahoWebPackageResourceMapping( this.context.getService( reference ) );

      Dictionary<String, String> serviceProperties = new Hashtable<>( 2 );
      serviceProperties.put( RESOURCE_PATTERN_KEY, mapping.getAlias() + "/*" );
      serviceProperties.put( RESOURCE_PREFIX_KEY, mapping.getPath() );

      return bundle.getBundleContext().registerService( String.class.getName(), "", serviceProperties );
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
