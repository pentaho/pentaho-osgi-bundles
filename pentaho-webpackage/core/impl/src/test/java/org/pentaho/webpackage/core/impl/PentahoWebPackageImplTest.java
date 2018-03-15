package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.Before;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.webpackage.core.PentahoWebPackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

public class PentahoWebPackageImplTest {

  private Bundle mockBundle;
  private BundleContext mockBundleContext;
  private String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
  private ServiceRegistration mockServiceReference;
  private URL mockUrl;
  private URLConnection mockUrlCon;
  private String name = "somePackageName";
  private PentahoWebPackageImpl pentahoWebPackage;
  private String resourceRootPath = "some/resource/path";
  private String version = "1.0";

  @Before
  public void setUp() {
    createMockUrlConnection();
    mockBundle = this.createMockBundle( "BundleName", "1.0", Bundle.ACTIVE );
    mockBundleContext = mockBundle.getBundleContext();
    pentahoWebPackage = new PentahoWebPackageImpl( mockBundleContext, name, version, resourceRootPath );
  }

  private void createMockUrlConnection() {
    mockUrlCon = mock( URLConnection.class );
    URLStreamHandler stubUrlHandler = null;
    try {
      ByteArrayInputStream is = new ByteArrayInputStream(
          "<myList></myList>".getBytes( "UTF-8" ) );
      doReturn( is ).when( mockUrlCon ).getInputStream();

      stubUrlHandler = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
          return mockUrlCon;
        }
      };

      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( mockPackageJson.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      mockUrl = new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( bundleState );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );

    mockServiceReference = mock( ServiceRegistration.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );
    when( mockBundleContext.registerService( eq( PentahoWebPackage.class.getName() ), any(), any() ) )
        .thenReturn( mockServiceReference );

    return mockBundle;
  }

  @Test
  public void getName() {
    assertEquals( "Should return Package name", name, pentahoWebPackage.getName() );
  }

  @Test
  public void getVersion() {
    assertEquals( "Should return Package version", version, pentahoWebPackage.getVersion() );
  }

  @Test
  public void getResourceRootPath() {
    assertEquals( "Should return Package Resource Root Path", resourceRootPath, pentahoWebPackage.getResourceRootPath() );
  }

  @Test
  public void getWebRootPath() {
    String webRootPath = "/" + this.name + "/" + this.version;
    assertEquals( "Should return WebPackage web root path", webRootPath, pentahoWebPackage.getWebRootPath() );
  }

  @Test
  public void getPackageJson() {
    Map<String, Object> jsonResponse = pentahoWebPackage.getPackageJson();
    Map<String, Object> jsonObjectMap = null;
    try {
      jsonObjectMap = (Map<String, Object>) new JSONParser().parse( mockPackageJson );
    } catch ( ParseException ignored ) {
    }
    assertEquals( "Should return valid JSON", jsonObjectMap, jsonResponse );
  }

  @Test
  public void getPackageJsonCatchException() {
    try {
      doThrow( new IOException() ).when( mockUrlCon ).getInputStream();
      pentahoWebPackage.getPackageJson();
    } catch ( IOException ignored ) {
    }
  }

  @Test
  public void init() {
    pentahoWebPackage.init();
    verify( mockBundleContext, times( 1 ) )
        .registerService( PentahoWebPackage.class.getName(), pentahoWebPackage, null );
  }

  @Test
  public void destroy() {
    pentahoWebPackage.init();
    pentahoWebPackage.destroy();
    verify( mockServiceReference, times( 1 ) ).unregister();
  }

  @Test
  public void destroyWhenServiceReferenceIsNull() {
    pentahoWebPackage.destroy();
  }

  @Test
  public void destroyWhenServiceAlreadyUnregistered() {
    doThrow( new RuntimeException() ).when( mockServiceReference ).unregister();
    pentahoWebPackage.init();
    pentahoWebPackage.destroy();
  }
}
