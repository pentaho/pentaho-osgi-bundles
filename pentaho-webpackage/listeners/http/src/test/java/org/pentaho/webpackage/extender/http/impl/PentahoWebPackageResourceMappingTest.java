package org.pentaho.webpackage.extender.http.impl;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.core.impl.PentahoWebPackageImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageResourceMappingTest {

  private PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping;
  private String mockPackageJson = "{\"name\":\"SomeName\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
  private BundleContext mockBundleContext;
  private IPentahoWebPackage pentahoWebPackage;
  private URL mockUrl;
  private String resourcesRootPath = "/some/path/to/web/root";

  @Before
  public void setUp() {
    mockBundleContext = mock( BundleContext.class );
    mockUrl = this.createMockUrlConnection( mockPackageJson );
    pentahoWebPackage = new PentahoWebPackageImpl( resourcesRootPath, mockUrl );
  }

  @Test
  public void testGetHttpContextIdShouldReturnNull() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // act
    String actualHttpContext = this.pentahoWebPackageResourceMapping.getHttpContextId();

    // assert
    assertNull( "Should be null", actualHttpContext );
  }

  @Test
  public void testGetAliasShouldReturnValidWebRootPath() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );
    String expectedWebRootPath = pentahoWebPackage.getWebRootPath();

    // act
    String actualWebRootPath = this.pentahoWebPackageResourceMapping.getAlias();

    // assert
    assertEquals( "Should return valid Webroot Path", expectedWebRootPath, actualWebRootPath );
  }

  @Test
  public void testGetPathShouldReturnValidResourcesRootPath() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );
    String expectedResourcesRootPath = this.resourcesRootPath;

    // act
    String actualResourcesRootPath = this.pentahoWebPackageResourceMapping.getPath();

    // assert
    assertEquals( "Should return valid Resources Root Path", expectedResourcesRootPath, actualResourcesRootPath );
  }

  @Test
  public void testToStringShouldReturnValidToString() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );
    String alias = pentahoWebPackage.getWebRootPath();
    String path = this.resourcesRootPath;
    String expectedToString = PentahoWebPackageResourceMapping.class.getSimpleName()
        + "{" + "alias=" + alias + ",path=" + path + "}";

    // act
    String actualToString = this.pentahoWebPackageResourceMapping.toString();

    // assert
    assertEquals( "Should Return Valid To String", expectedToString, actualToString );
  }

  @Test
  public void testEqualsWhereObjectNull() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // assert
    assertFalse( this.pentahoWebPackageResourceMapping.equals( null ) );
  }

  @Test
  public void testEqualsWhereObjectIsInvalidReference() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // act
    Object invalidRef = new Object();

    // assert
    assertFalse( this.pentahoWebPackageResourceMapping.equals( invalidRef ) );
  }

  @Test
  public void testEqualsWhereObjectIsValidReference() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // act
    Object validRef = this.pentahoWebPackageResourceMapping;

    // assert
    assertTrue( this.pentahoWebPackageResourceMapping.equals( validRef ) );
  }

  @Test
  public void testEqualsWhereObjectIsValidReferenceOther() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // act
    Object other =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // assert
    assertTrue( this.pentahoWebPackageResourceMapping.equals( other ) );
  }

  @Test
  public void testRegisterShouldCallRegisterServiceOnce() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );

    // act
    this.pentahoWebPackageResourceMapping.register();

    // assert
    verify( mockBundleContext, times( 1 ) )
        .registerService( ResourceMapping.class, this.pentahoWebPackageResourceMapping, null );
  }
  
  @Test
  public void testUnregisterShouldCallServiceRegistrationUnregisterOnce() throws Exception {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockBundleContext, this.pentahoWebPackage );
    ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
    doReturn( mockServiceRegistration ).when( this.mockBundleContext )
        .registerService( ResourceMapping.class, this.pentahoWebPackageResourceMapping, null );

    // act
    this.pentahoWebPackageResourceMapping.register();
    this.pentahoWebPackageResourceMapping.unregister();

    // assert
    verify( mockServiceRegistration, times( 1 ) )
        .unregister();
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