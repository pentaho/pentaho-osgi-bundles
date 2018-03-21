package org.pentaho.webpackage.extender.requirejs.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageServiceTrackerTest {

  private PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker;

  @Before
  public void setUp() {
  }

  @Test
  public void testAddingServiceWithValidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );


    // act
    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNotNull( "Should return valid IRequireJsPackage object", requireJsPackage );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( null ).when( mockServiceReference ).getBundle();

    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );


    // act
    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( "Should return null", requireJsPackage );
  }

  // This test is just for coverage
  @Test
  public void testModifiedService() throws Exception {
    // arrange
    BundleContext mockBundleContext = mock( BundleContext.class );
    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    this.pentahoWebPackageServiceTracker.modifiedService( null, null );

    // assert
  }

  @Test
  public void removedService() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );

    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    IRequireJsPackage mockRequireJsPackage = spy( requireJsPackage );

    // act
    this.pentahoWebPackageServiceTracker.removedService( mockServiceReference, mockRequireJsPackage );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( any() );
    verify( mockRequireJsPackage, times( 1 ) ).unregister();
  }

  private Bundle createBaseMockBundle() {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( "SomeBundleName" );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( "1.1.1.1" );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( Bundle.ACTIVE );

    ServiceRegistration mockServiceReference = mock( ServiceRegistration.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );
    when( mockBundleContext.registerService( eq( IPentahoWebPackage.class.getName() ), any(), any() ) )
        .thenReturn( mockServiceReference );

    return mockBundle;

  }

}