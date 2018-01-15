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
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class PentahoWebPackageImpl extends PentahoWebPackageAbstract {
  private final BundleContext bundleContext;

  private ServiceRegistration<?> serviceReference;

  PentahoWebPackageImpl( Bundle bundle, String name, String version, String resourceRootPath ) {
    super( name, version, resourceRootPath );

    this.bundleContext = bundle.getBundleContext();
  }

  @Override
  public void init() {
    // Register resource mapping in httpService whiteboard
    DefaultResourceMapping resourceMapping = new DefaultResourceMapping();
    resourceMapping.setAlias( "/" + this.getName() + "/" + this.getVersion() );
    resourceMapping.setPath( this.getResourceRootPath() );

    this.serviceReference = this.bundleContext.registerService( ResourceMapping.class.getName(), resourceMapping, null );
  }

  @Override
  public void destroy() {
    if ( this.serviceReference != null ) {
      try {
        this.serviceReference.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.serviceReference = null;
    }
  }
}
