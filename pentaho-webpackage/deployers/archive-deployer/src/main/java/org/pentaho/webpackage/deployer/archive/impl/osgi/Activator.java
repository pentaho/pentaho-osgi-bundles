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
