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
package org.pentaho.webpackage.extender.http.impl;

import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.IPentahoWebPackage;

/**
 * A webpackage targeted implementation of {@link ResourceMapping}.
 *
 * Also provides methods to register and unregister itself.
 */
public class PentahoWebPackageResourceMapping implements ResourceMapping {
  private final BundleContext bundleContext;

  private final IPentahoWebPackage pentahoWebPackage;

  private ServiceRegistration<ResourceMapping> serviceRegistration;

  PentahoWebPackageResourceMapping( BundleContext bundleContext, IPentahoWebPackage pentahoWebPackage ) {
    super();

    this.bundleContext = bundleContext;
    this.pentahoWebPackage = pentahoWebPackage;
  }

  @Override
  public String getHttpContextId() {
    return null;
  }

  @Override
  public String getAlias() {
    return this.pentahoWebPackage.getWebRootPath();
  }

  @Override
  public String getPath() {
    return this.pentahoWebPackage.getResourceRootPath();
  }

  public String toString() {
    return this.getClass().getSimpleName() + "{" + "alias=" + this.getAlias() + ",path=" + this.getPath() + "}";
  }

  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    } else if ( obj == null ) {
      return false;
    } else if ( this.getClass() != obj.getClass() ) {
      return false;
    } else {
      PentahoWebPackageResourceMapping other = (PentahoWebPackageResourceMapping) obj;

      return other.pentahoWebPackage.equals( this.pentahoWebPackage );
    }
  }

  public void register() {
    // Register resource mapping in httpService whiteboard
    this.serviceRegistration = this.bundleContext.registerService( ResourceMapping.class, this, null );
  }

  public void unregister() {
    if ( this.serviceRegistration != null ) {
      try {
        this.serviceRegistration.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.serviceRegistration = null;
    }
  }
}
