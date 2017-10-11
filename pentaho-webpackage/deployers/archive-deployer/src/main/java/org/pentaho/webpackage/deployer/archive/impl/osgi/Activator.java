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
package org.pentaho.webpackage.deployer.archive.impl.osgi;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.pentaho.webpackage.deployer.archive.impl.WebPackageURLConnection;
import org.pentaho.webpackage.deployer.archive.impl.UrlHandler;
import org.pentaho.webpackage.deployer.archive.impl.UrlTransformer;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {
  private UrlTransformer urlTransformer;
  private ServiceRegistration<ArtifactUrlTransformer> urlTransformerRegistration;

  private UrlHandler urlHandler;
  private ServiceRegistration<URLStreamHandlerService> urlHandlerRegistration;

  public void start( BundleContext bundleContext ) {
    this.urlTransformer = new UrlTransformer();
    this.urlTransformerRegistration = bundleContext.registerService( ArtifactUrlTransformer.class, this.urlTransformer, null );

    this.urlHandler = new UrlHandler();

    Dictionary<String, String> props = new Hashtable<>();
    props.put( URLConstants.URL_HANDLER_PROTOCOL, WebPackageURLConnection.URL_PROTOCOL );

    this.urlHandlerRegistration = bundleContext.registerService( URLStreamHandlerService.class, this.urlHandler, props );
  }

  public void stop( BundleContext bundleContext ) {
    if ( this.urlTransformerRegistration != null ) {
      try {
        this.urlTransformerRegistration.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.urlTransformerRegistration = null;
      this.urlTransformer = null;
    }

    if ( this.urlHandlerRegistration != null ) {
      try {
        this.urlHandlerRegistration.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.urlHandlerRegistration = null;
      this.urlHandler = null;
    }
  }
}
