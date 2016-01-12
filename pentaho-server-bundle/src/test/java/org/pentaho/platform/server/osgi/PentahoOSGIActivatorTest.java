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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.server.osgi;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
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