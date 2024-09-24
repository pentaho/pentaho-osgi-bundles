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
