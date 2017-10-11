/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2017 Hitachi Vantara. All rights reserved.
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
