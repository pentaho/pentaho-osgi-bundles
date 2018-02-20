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
package org.pentaho.webpackage.extender.http.impl.osgi;

public class ActivatorTest {
//  private Activator activator;
//
//  @Before
//  public void setUp() throws Exception {
//    this.activator = new Activator();
//  }
//
//  @Test
//  public void start() throws Exception {
//    BundleContext mockBundleContext = mock( BundleContext.class );
//
//    this.activator.start( mockBundleContext );
//
//    verify( mockBundleContext, times( 1 ) ).registerService( eq( PentahoWebPackageBundleListener.class ), any( PentahoWebPackageBundleListener.class ), any() );
//    verify( mockBundleContext, times( 1 ) ).addBundleListener( any( BundleListener.class ) );
//  }
//
//  @Test
//  public void startAddsAlreadyStartedBundles() throws Exception {
//    this.activator = spy( this.activator );
//
//    PentahoWebPackageBundleListener mockPentahoWebPackageService = mock( PentahoWebPackageBundleListener.class );
//
//    when(this.activator.createPentahoWebPackageService()).thenReturn( mockPentahoWebPackageService );
//
//    BundleContext mockBundleContext = mock( BundleContext.class );
//
//    Bundle[] bundles = new Bundle[3];
//    bundles[0] = mock( Bundle.class );
//    when( bundles[0].getState() ).thenReturn( Bundle.ACTIVE );
//    bundles[1] = mock( Bundle.class );
//    when( bundles[1].getState() ).thenReturn( Bundle.INSTALLED );
//    bundles[2] = mock( Bundle.class );
//    when( bundles[2].getState() ).thenReturn( Bundle.ACTIVE );
//
//    when( mockBundleContext.getBundles() ).thenReturn( bundles );
//
//    this.activator.start( mockBundleContext );
//
//    verify( mockPentahoWebPackageService, times( 1 ) ).addBundle( same( bundles[0] ) );
//    verify( mockPentahoWebPackageService, never() ).addBundle( same( bundles[1] ) );
//    verify( mockPentahoWebPackageService, times( 1 ) ).addBundle( same( bundles[2] ) );
//  }
//
//  @Test
//  public void stop() throws Exception {
//    ArgumentCaptor<BundleListener> bundleListenerCaptor = ArgumentCaptor.forClass( BundleListener.class );
//
//    BundleContext mockBundleContext = mock( BundleContext.class );
//    final ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
//    when( mockBundleContext
//        .registerService( eq( PentahoWebPackageBundleListener.class ), any( PentahoWebPackageBundleListener.class ), any() ) )
//        .thenReturn( mockServiceRegistration );
//
//    this.activator.start( mockBundleContext );
//
//    verify( mockBundleContext, times( 1 ) ).addBundleListener( bundleListenerCaptor.capture() );
//
//    this.activator.stop( mockBundleContext );
//
//    verify( mockServiceRegistration, times( 1 ) ).unregister();
//    verify( mockBundleContext, times( 1 ) ).removeBundleListener( same( bundleListenerCaptor.getValue() ) );
//  }
}
