package org.pentaho.webpackage.extender.http.impl;

import org.junit.Test;
import org.junit.Before;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.pentaho.webpackage.core.impl.PentahoWebPackageImpl;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class PentahoWebPackageImplTest {

  private Bundle mockBundle;
  private PentahoWebPackageImpl pentahoWebPackage;
  private String name = "somePackageName";
  private String version = "1.0";
  private String resourceRootPath = "some/resource/path";
  private URL mockUrl;
  //  private BundleContext bundleContext;
  //  private PentahoWebPackageImpl mockPentahoWebPackage;

  @Before
  public void setUp() {
    try {
      mockUrl = new URL("http://someurl.com");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    mockBundle = this.createMockBundle("SomeBundleName", "1.0", Bundle.ACTIVE);
    pentahoWebPackage = new PentahoWebPackageImpl(mockBundle, name, version, resourceRootPath);

  }

  @Test
  public void getName() {
    assertEquals("Should return Package name", name, pentahoWebPackage.getName());
  }

  @Test
  public void getVersion() {
    assertEquals("Should return Package version", version, pentahoWebPackage.getVersion());
  }

  @Test
  public void getResourceRootPath() {
    assertEquals("Should return Package Resource Root Path", resourceRootPath, pentahoWebPackage.getResourceRootPath());
  }

  @Test
  public void getWebRootPath(){
    String webRootPath = "/" + this.name + "/" + this.version;
    assertEquals("Should return WebPackage web root path", webRootPath, pentahoWebPackage.getWebRootPath());
  }

  @Test
  public void getPackageJsonResource(){
    String scriptPath = this.resourceRootPath + "/package.json";
    assertSame("Should return resource URL", this.mockUrl, mockBundle.getResource( scriptPath ));
  }

  @Test
  public void init(){

  }

  @Test
  public void destroy(){

  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( bundleState );
    when( mockBundle.getResource( eq(this.resourceRootPath + "/package.json") ) )
        .thenReturn( this.mockUrl );
    return mockBundle;
  }
}
