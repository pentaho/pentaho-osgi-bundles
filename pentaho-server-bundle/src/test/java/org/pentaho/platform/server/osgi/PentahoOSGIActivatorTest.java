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
package org.pentaho.platform.server.osgi;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContext;
import java.util.Dictionary;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PentahoOSGIActivatorTest {

  @Test
  @SuppressWarnings( "unchecked" )
  public void testStart() throws Exception {
    BundleContext bundleContext = mock( BundleContext.class );
    ServiceReference<ConfigurationAdmin> adminRef = (ServiceReference<ConfigurationAdmin>) mock( ServiceReference.class);
    when( bundleContext.getServiceReference( ConfigurationAdmin.class )).thenReturn( adminRef );

    ConfigurationAdmin configurationAdmin = mock(ConfigurationAdmin.class);
    when( bundleContext.getService( adminRef )).thenReturn( configurationAdmin );

    Configuration config = mock( Configuration.class );
    when(configurationAdmin.getConfiguration( "org.pentaho.requirejs", null )).thenReturn( config );

    IApplicationContext applicationContext = mock(IApplicationContext.class);
    PentahoSystem.init(applicationContext);

    when(applicationContext.getFullyQualifiedServerURL()).thenReturn("http://localhost:8080/pentaho");
    when(applicationContext.getContext()).thenReturn( mock( ServletContext.class ) );

    ArgumentCaptor<Dictionary> params = ArgumentCaptor.forClass( Dictionary.class );

    PentahoOSGIActivator activator = new PentahoOSGIActivator();
    activator.start( bundleContext );

    verify( config, times( 1 ) ).update( params.capture() );
    assertEquals("/pentaho/osgi/", params.getValue().get( "context.root" ));

    reset(config);

    when(applicationContext.getFullyQualifiedServerURL()).thenReturn("http://localhost:8080/");
    activator.start( bundleContext );
    verify( config, times( 1 ) ).update( params.capture() );
    assertEquals("/osgi/", params.getValue().get( "context.root" ));

    activator.stop( bundleContext );

  }
}