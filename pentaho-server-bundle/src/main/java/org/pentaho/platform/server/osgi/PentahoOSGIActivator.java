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

package org.pentaho.platform.server.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Standard OSGI Activator class which is called when the OSGI environment is started. Work to integrate the OSGI
 * container with the PentahoSystem is started from this class
 */
public class PentahoOSGIActivator implements BundleActivator {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public void start( BundleContext bundleContext ) throws Exception {

    IApplicationContext applicationContext = PentahoSystem.getApplicationContext();

    Object innerContext = applicationContext.getContext();
    if ( innerContext instanceof ServletContext ) {

      ServletContext servletContext = (ServletContext) innerContext;
      servletContext.setAttribute( BundleContext.class.getName(), bundleContext );

      updateRootContextInConfigurationAdmin( bundleContext, applicationContext );
    }
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }


  private void updateRootContextInConfigurationAdmin( BundleContext bundleContext,
                                                      IApplicationContext webApplicationContext ) throws IOException {

    ServiceReference<ConfigurationAdmin> configurationAdminServiceReference =
        bundleContext.getServiceReference( ConfigurationAdmin.class );
    ConfigurationAdmin configurationAdmin = bundleContext.getService( configurationAdminServiceReference );

    // guaranteed to not be null
    Configuration configuration = configurationAdmin.getConfiguration( "org.pentaho.requirejs", null );
    Dictionary<String, Object> properties = configuration.getProperties();
    if ( properties == null ) {
      // new config
      properties = new Hashtable<String, Object>();
    }
    String fullyQualifiedServerURL = webApplicationContext.getFullyQualifiedServerURL();
    URL url = new URL( fullyQualifiedServerURL );
    // extract webapp path if it exists
    String webappPath = url.getPath();
    if ( webappPath == null || "".equals( webappPath ) ) {

      webappPath = "/";
    } else {
      // non-root webapp, make sure it's surrounded by /'s
      if ( webappPath.endsWith( "/" ) == false ) {
        webappPath += "/";
      }
      if ( webappPath.startsWith( "/" ) == false ) {
        webappPath = "/" + webappPath;
      }
    }

    webappPath = webappPath + "osgi/"; // OSGI Bridge Servlet path, defined in web.xml
    properties.put( "context.root", webappPath );
    configuration.update( properties );
  }

}
