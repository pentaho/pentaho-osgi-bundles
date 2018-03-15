package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.PentahoWebPackage;
import org.pentaho.webpackage.core.PentahoWebPackageBundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class PentahoWebPackageBundleImplTest {

  private Bundle mockBundle;
  private BundleContext mockBundleContext;
  private String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
  private ServiceRegistration mockServiceReference;
  private URL mockUrl;
  private URLConnection mockUrlCon;
  private PentahoWebPackageBundleImpl pentahoWebPackageBundle;
  private String resourceRootPath = "some/resource/path";

  @Before
  public void setUp() {
    mockBundle = this.createMockBundle( "BundleName", "1.0", Bundle.ACTIVE );
//    mockBundleContext = mockBundle.getBundleContext();
    pentahoWebPackageBundle = new PentahoWebPackageBundleImpl( mockBundle );
  }

  // TODO: duplicate code in PentahoWebPackageImplTest
  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( bundleState );
//    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
//        .thenReturn( this.mockUrl );
    mockServiceReference = mock( ServiceRegistration.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );
    when( mockBundleContext.registerService( eq( PentahoWebPackage.class.getName() ), any(), any() ) )
        .thenReturn( mockServiceReference );

    return mockBundle;
  }

  @Test
  public void getCapabilities() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageBundle.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
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
    createMockUrlConnection( mockPackageJson );
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

  private void createMockUrlConnection( String payload ) {
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

      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( payload.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      mockUrl = new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void getWebPackages() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageBundle.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    List<BundleCapability> capabilities = null;

    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    createMockUrlConnection( mockPackageJson );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );

    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );

    bundleCapabilityList.add( mockBundleCapability );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 1, capabilities.size() );
    List<PentahoWebPackageImpl> webPackages = pentahoWebPackageBundle.getWebPackages( capabilities );

    // Assert
    assertEquals( 1, webPackages.size() );
  }

  @Test
  public void getWebPackagesThrowException() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageBundle.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    List<BundleCapability> capabilities = null;

    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    createMockUrlConnection( "some invalid json data" );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( this.mockUrl );

    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );

    bundleCapabilityList.add( mockBundleCapability );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 1, capabilities.size() );
    List<PentahoWebPackageImpl> webPackages = pentahoWebPackageBundle.getWebPackages( capabilities );

    // Assert returned collection is empty
    assertEquals( 0, webPackages.size() );
  }

  @Test
  public void getWebPackagesCantGetBundleResource() {
    BundleWiring mockBundleWiring = mock( BundleWiring.class );

    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();

    when( mockBundleWiring.getCapabilities( PentahoWebPackageBundle.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    List<BundleCapability> capabilities = null;

    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    createMockUrlConnection( "some invalid json data" );
    when( mockBundle.getResource( eq( this.resourceRootPath + "/package.json" ) ) )
        .thenReturn( null );

    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );

    bundleCapabilityList.add( mockBundleCapability );
    capabilities = pentahoWebPackageBundle.getCapabilities();
    assertEquals( 1, capabilities.size() );
    List<PentahoWebPackageImpl> webPackages = pentahoWebPackageBundle.getWebPackages( capabilities );

    // Assert returned collection is empty
    assertEquals( 0, webPackages.size() );
  }


  @Test
  public void init() {
    PentahoWebPackageImpl pentahoWebPackage = spy( new PentahoWebPackageImpl( mockBundleContext, "", "", "" ) );
    List<PentahoWebPackageImpl> packages = new ArrayList<>();
    packages.add( spy( new PentahoWebPackageImpl( mockBundleContext, "", "", "" ) ) );
    packages.add( spy( new PentahoWebPackageImpl( mockBundleContext, "", "", "" ) ) );
    packages.add( spy( new PentahoWebPackageImpl( mockBundleContext, "", "", "" ) ) );
    pentahoWebPackageBundle.setPentahoWebPackages( packages );
    pentahoWebPackageBundle.init();
    verify( pentahoWebPackage, times( 1 ) ).init();
  }

  @Test
  public void destroy() {

  }
}
