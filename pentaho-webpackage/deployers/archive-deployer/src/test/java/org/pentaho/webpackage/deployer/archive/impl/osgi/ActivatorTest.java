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
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
