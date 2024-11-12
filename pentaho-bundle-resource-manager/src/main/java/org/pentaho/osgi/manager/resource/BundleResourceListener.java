/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.manager.resource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.pentaho.osgi.manager.resource.api.ResourceHandler;

/**
 * This is a bundle listener that gets the callback whenever the bundle state changes.
 */
public class BundleResourceListener implements BundleListener {
  private BundleContext bundleContext;
  private ResourceHandler resourceHandler;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void setResourceHandler( ResourceHandler resourceHandler ) {
    this.resourceHandler = resourceHandler;
  }

  @Override public void bundleChanged( BundleEvent event ) {
    switch ( event.getType() ) {
      case BundleEvent.RESOLVED:
        Bundle bundle = event.getBundle();
        if ( bundle != null && resourceHandler.hasManagedResources( bundle ) ) {
          resourceHandler.handleManagedResources( bundle );
        }
        break;
    }
  }

  public void init() throws Exception {
    bundleContext.addBundleListener( this );
  }
}
