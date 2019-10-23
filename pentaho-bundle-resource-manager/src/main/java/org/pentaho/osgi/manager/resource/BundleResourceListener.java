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
