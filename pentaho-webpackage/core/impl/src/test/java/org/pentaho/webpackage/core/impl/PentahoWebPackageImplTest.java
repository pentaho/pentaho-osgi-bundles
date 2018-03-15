/*
package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    mockServiceReference = mock( ServiceRegistration.class );
    mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    try {
      mockUrlCon = mockUrl.openConnection();
    } catch ( IOException ignored ) {
    }
    mockBundle = this.createMockBundle( "BundleName", "1.0", Bundle.ACTIVE );
    mockBundleContext = mockBundle.getBundleContext();
    pentahoWebPackage = new PentahoWebPackageImpl( mockBundleContext, name, version, resourceRootPath );
  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = TestUtils.createBaseMockBundle( bundleName, bundleVersion, bundleState, mockServiceReference );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );
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
        .registerService( IPentahoWebPackage.class.getName(), pentahoWebPackage, null );
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
  */
