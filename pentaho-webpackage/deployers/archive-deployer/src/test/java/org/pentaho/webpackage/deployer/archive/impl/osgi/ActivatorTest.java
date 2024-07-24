/*!
 * Copyright 2018-2024 Hitachi Vantara.  All rights reserved.
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
