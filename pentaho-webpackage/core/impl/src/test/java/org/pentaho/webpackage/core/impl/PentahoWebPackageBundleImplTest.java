package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.PentahoWebPackageConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageBundleImplTest {

  private Bundle mockBundle;
  private BundleContext mockBundleContext;
  private String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
  private ServiceRegistration mockServiceReference;
  private URL mockUrl;
  private PentahoWebPackageBundleImpl pentahoWebPackageBundle;
  private String resourceRootPath = "some/resource/path";

  @Before
  public void setUp() {
    mockBundle = this.createMockBundle( "BundleName", "1.0", Bundle.ACTIVE );
    pentahoWebPackageBundle = spy( new PentahoWebPackageBundleImpl( mockBundle ) );
  }


  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    mockServiceReference = mock( ServiceRegistration.class );
    Bundle mockBundle = TestUtils.createBaseMockBundle( bundleName, bundleVersion, bundleState, mockServiceReference );
    return mockBundle;
  }

  @Test
  public void getCapabilities() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    List<BundleCapability> capabilities = null;

    // Assert Empty collection when BundleWiring is null
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( null );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 0, capabilities.size() );


    // Assert BundleWiring has no Capabilities (empty collection)
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );
    assertEquals( 0, capabilities.size() );

    // Assert BundleWiring has 1 Capabilities (non empty collection)
    bundleCapabilityList.add( mockBundleCapability );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 1, capabilities.size() );

  }

  @Test
  public void parsePackageJson() {
    mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );
    Map<String, Object> jsonResponse = null;
    try {
      Method m = PentahoWebPackageBundleImpl.class.getDeclaredMethod( "parsePackageJson", URL.class );
      m.setAccessible( true );
      jsonResponse = (Map<String, Object>) m.invoke( null, mockUrl );
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      e.printStackTrace();
    }
    Map<String, Object> jsonObjectMap = null;
    try {
      jsonObjectMap = (Map<String, Object>) new JSONParser().parse( mockPackageJson );
    } catch ( ParseException ignored ) {
    }
    assertEquals( "Should return valid JSON", jsonObjectMap, jsonResponse );
  }


  @Test
  public void getWebPackages() {
    mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );
    List<PentahoWebPackageImpl> webPackages = setupGetWebPackages();
    // Assert returned collection has the webpackage passed
    assertEquals( 1, webPackages.size() );
  }

  @Test
  public void getWebPackagesThrowException() {
    mockUrl = TestUtils.createMockUrlConnection( "some invalid json data" );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );
    List<PentahoWebPackageImpl> webPackages = setupGetWebPackages();
    // Assert returned collection is empty
    assertEquals( 0, webPackages.size() );
  }

  @Test
  public void getWebPackagesCantGetBundleResource() {
    mockUrl = TestUtils.createMockUrlConnection( "some invalid json data" );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( null );
    List<PentahoWebPackageImpl> webPackages = setupGetWebPackages();

    // Assert returned collection is empty
    assertEquals( 0, webPackages.size() );
  }

  private List<PentahoWebPackageImpl> setupGetWebPackages() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    List<BundleCapability> capabilities = null;

    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );

    bundleCapabilityList.add( mockBundleCapability );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 1, capabilities.size() );
    return pentahoWebPackageBundle.getWebPackages( capabilities );
  }


  @Test
  public void init() {
    PentahoWebPackageImpl pentahoWebPackage = getPentahoWebPackage();
    pentahoWebPackageBundle.init();
    verify( pentahoWebPackage, times( 3 ) ).init();
  }

  @Test
  public void destroy() {
    PentahoWebPackageImpl pentahoWebPackage = getPentahoWebPackage();
    pentahoWebPackageBundle.init();
    pentahoWebPackageBundle.destroy();
    verify( pentahoWebPackage, times( 3 ) ).destroy();
  }

  private PentahoWebPackageImpl getPentahoWebPackage() {
    mockBundleContext = mockBundle.getBundleContext();
    PentahoWebPackageImpl pentahoWebPackage = spy( new PentahoWebPackageImpl( mockBundleContext, "", "", "" ) );
    List<PentahoWebPackageImpl> packages = new ArrayList<>();
    packages.add( pentahoWebPackage );
    packages.add( pentahoWebPackage );
    packages.add( pentahoWebPackage );
    doReturn( packages ).when( pentahoWebPackageBundle ).getWebPackages( any() );
    return pentahoWebPackage;
  }
}
