package org.pentaho.webpackage.extender.http.impl;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.core.impl.PentahoWebPackageImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageServiceTrackerTest {

  private String mockPackageJson = "{\"name\":\"SomeName\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";

  @Test
  public void testAddingServiceWithValidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping =
        pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNotNull( pentahoWebPackageResourceMapping );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = null;
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();
    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping =
        pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( pentahoWebPackageResourceMapping );
  }

  // This test is just for coverage
  @Test
  public void modifiedService() throws Exception {
    // arrange
    BundleContext mockBundleContext = mock( BundleContext.class );
    PentahoWebPackageResourceMapping mockPentahoWebPackageResourceMapping = mock( PentahoWebPackageResourceMapping.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    pentahoWebPackageServiceTracker.modifiedService( mockServiceReference, mockPentahoWebPackageResourceMapping );
  }

  @Test
  public void testRemovedServiceShouldCallBundleContextUngetServiceAndPentahoWebPackageResourceMappingUnregisterOnce() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    PentahoWebPackageResourceMapping mockPentahoWebPackageResourceMapping = mock( PentahoWebPackageResourceMapping.class );

    // act
    pentahoWebPackageServiceTracker.removedService( mockServiceReference, mockPentahoWebPackageResourceMapping );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( mockServiceReference );
    verify( mockPentahoWebPackageResourceMapping, times( 1 ) ).unregister();
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

  private URL createMockUrlConnection( String payload ) {
    URLConnection mockUrlCon = mock( URLConnection.class );
    URLStreamHandler stubUrlHandler = null;
    try {
      stubUrlHandler = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
          return mockUrlCon;
        }
      };
      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( payload.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }
}