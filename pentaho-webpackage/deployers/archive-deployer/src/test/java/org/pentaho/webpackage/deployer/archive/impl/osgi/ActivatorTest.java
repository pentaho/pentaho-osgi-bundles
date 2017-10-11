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
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActivatorTest {
  private Activator activator;

  @Before
  public void setUp() throws Exception {
    this.activator = new Activator();
  }

  @Test
  public void start() throws Exception {
    BundleContext mockBundleContext = mock( BundleContext.class );

    this.activator.start( mockBundleContext );

    verify( mockBundleContext, times( 1 ) ).registerService( eq( ArtifactUrlTransformer.class ), any( ArtifactUrlTransformer.class ), any() );
    verify( mockBundleContext, times( 1 ) ).registerService( eq( URLStreamHandlerService.class ), any( URLStreamHandlerService.class ), any() );
  }

  @Test
  public void stop() throws Exception {
    BundleContext mockBundleContext = mock( BundleContext.class );

    final ServiceRegistration mockArtifactUrlTransformerRegistration = mock( ServiceRegistration.class );
    when( mockBundleContext
        .registerService( eq( ArtifactUrlTransformer.class ), any( ArtifactUrlTransformer.class ), any() ) )
        .thenReturn( mockArtifactUrlTransformerRegistration );

    final ServiceRegistration mockURLStreamHandlerServiceRegistration = mock( ServiceRegistration.class );
    when( mockBundleContext
        .registerService( eq( URLStreamHandlerService.class ), any( URLStreamHandlerService.class ), any() ) )
        .thenReturn( mockURLStreamHandlerServiceRegistration );

    this.activator.start( mockBundleContext );

    this.activator.stop( mockBundleContext );

    verify( mockArtifactUrlTransformerRegistration, times( 1 ) ).unregister();
    verify( mockURLStreamHandlerServiceRegistration, times( 1 ) ).unregister();
  }
}
