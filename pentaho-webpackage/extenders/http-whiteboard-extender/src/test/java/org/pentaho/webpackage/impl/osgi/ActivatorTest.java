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
 * Copyright 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.webpackage.impl.osgi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.PentahoWebPackageService;
import org.pentaho.webpackage.impl.PentahoWebPackageServiceImpl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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

    verify( mockBundleContext, times( 1 ) ).registerService( eq( PentahoWebPackageService.class ), any( PentahoWebPackageService.class ), any() );
    verify( mockBundleContext, times( 1 ) ).addBundleListener( any( BundleListener.class ) );
  }

  @Test
  public void startAddsAlreadyStartedBundles() throws Exception {
    this.activator = spy( this.activator );

    PentahoWebPackageServiceImpl mockPentahoWebPackageService = mock( PentahoWebPackageServiceImpl.class );

    when(this.activator.createPentahoWebPackageService()).thenReturn( mockPentahoWebPackageService );

    BundleContext mockBundleContext = mock( BundleContext.class );

    Bundle[] bundles = new Bundle[3];
    bundles[0] = mock( Bundle.class );
    when( bundles[0].getState() ).thenReturn( Bundle.ACTIVE );
    bundles[1] = mock( Bundle.class );
    when( bundles[1].getState() ).thenReturn( Bundle.INSTALLED );
    bundles[2] = mock( Bundle.class );
    when( bundles[2].getState() ).thenReturn( Bundle.ACTIVE );

    when( mockBundleContext.getBundles() ).thenReturn( bundles );

    this.activator.start( mockBundleContext );

    verify( mockPentahoWebPackageService, times( 1 ) ).addBundle( same( bundles[0] ) );
    verify( mockPentahoWebPackageService, never() ).addBundle( same( bundles[1] ) );
    verify( mockPentahoWebPackageService, times( 1 ) ).addBundle( same( bundles[2] ) );
  }

  @Test
  public void stop() throws Exception {
    ArgumentCaptor<BundleListener> bundleListenerCaptor = ArgumentCaptor.forClass( BundleListener.class );

    BundleContext mockBundleContext = mock( BundleContext.class );
    final ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
    when( mockBundleContext
        .registerService( eq( PentahoWebPackageService.class ), any( PentahoWebPackageService.class ), any() ) )
        .thenReturn( mockServiceRegistration );

    this.activator.start( mockBundleContext );

    verify( mockBundleContext, times( 1 ) ).addBundleListener( bundleListenerCaptor.capture() );

    this.activator.stop( mockBundleContext );

    verify( mockServiceRegistration, times( 1 ) ).unregister();
    verify( mockBundleContext, times( 1 ) ).removeBundleListener( same( bundleListenerCaptor.getValue() ) );
  }
}
