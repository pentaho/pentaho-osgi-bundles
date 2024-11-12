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

package org.pentaho.webpackage.extender.requirejs.impl.osgi;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PentahoWebPackageServiceTrackerTest {
  private Bundle mockBundle;
  private BundleContext mockBundleContext;
  private ServiceReference mockServiceReference;
  private ServiceRegistration mockServiceRegistration;
  private IPentahoWebPackage mockPentahoWebPackage;
  private URL resourceRootUrl;

  private PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker;

  @Before
  public void setUp() {
    this.mockBundle = mock( Bundle.class );

    this.mockBundleContext = mock( BundleContext.class );

    this.mockServiceReference = mock( ServiceReference.class );
    doReturn( this.mockBundle ).when( mockServiceReference ).getBundle();
    doReturn( this.mockBundleContext ).when( this.mockBundle ).getBundleContext();

    this.mockServiceRegistration = mock( ServiceRegistration.class );
    doReturn( this.mockServiceRegistration ).when( this.mockBundleContext ).registerService( anyString(), any(), eq( null ) );

    this.mockPentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( this.mockPentahoWebPackage ).when( this.mockBundleContext ).getService( any() );

    this.resourceRootUrl = this.getClass().getResource( "/" );
    doReturn( this.resourceRootUrl ).when( this.mockBundle ).getResource( anyString() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( this.mockBundleContext );
  }

  @Test
  public void testAddingServiceWithValidServiceReference() {
    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( this.mockBundleContext );

    // act
    ServiceRegistration serviceRegistration = this.pentahoWebPackageServiceTracker.addingService( this.mockServiceReference );

    // assert
    assertNotNull( "Should return valid ServiceRegistration object", serviceRegistration );

    verify( this.mockBundleContext, times( 1 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), eq( null ) );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() {
    // arrange
    doReturn( null ).when( this.mockServiceReference ).getBundle();

    // act
    ServiceRegistration serviceRegistration = this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( "Should return null", serviceRegistration );

    verify( mockBundleContext, times( 0 ) ).registerService( anyString(), any(), eq( null ) );
  }

  @Test
  public void removedService() {
    // arrange
    ServiceRegistration serviceRegistration = this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // act
    this.pentahoWebPackageServiceTracker.removedService( this.mockServiceReference, serviceRegistration );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( any() );
    verify( serviceRegistration, times( 1 ) ).unregister();
  }
}
