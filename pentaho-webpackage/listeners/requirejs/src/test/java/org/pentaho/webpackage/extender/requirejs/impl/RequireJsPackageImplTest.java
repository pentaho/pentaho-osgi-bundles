/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.webpackage.extender.requirejs.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequireJsPackageImplTest {

  private RequireJsPackageImpl requireJsPackage;
  private BundleContext mockBundleContext;
  private IPentahoWebPackage mockPentahoWebPackage;


  @Before
  public void setUp() {
    mockBundleContext = mock( BundleContext.class );
    mockPentahoWebPackage = mock( IPentahoWebPackage.class );
  }

  @Test
  public void testGetName() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    String actualName = this.requireJsPackage.getName();

    // assert
    assertEquals( "Names should be equal", expectedPackageName, actualName );
  }

  @Test
  public void testGetVersion() {
    // arrange
    String expectedPackageVersion = "1.2.3";
    when( mockPentahoWebPackage.getVersion() ).thenReturn( expectedPackageVersion );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    String actualPackageVersion = this.requireJsPackage.getVersion();

    // assert
    assertEquals( "Versions should be equal", expectedPackageVersion, actualPackageVersion );
  }

  @Test
  public void testGetWebRootPath() {
    // arrange
    String expectedWebRootPath = "some/path/to/web/root";
    when( mockPentahoWebPackage.getWebRootPath() ).thenReturn( expectedWebRootPath );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    String actualWebRootPath = this.requireJsPackage.getWebRootPath();

    // assert
    assertEquals( "WebRootPaths should be equal", expectedWebRootPath, actualWebRootPath );
  }

  @Test
  public void testPreferGlobal() {
    // arrange
    boolean expectedPreferGlobal = true;
    Map<String, Object> preferGlobalJson = new HashMap<>();
    preferGlobalJson.put( "preferGlobal", expectedPreferGlobal );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( preferGlobalJson );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    boolean actualPreferGlobal = this.requireJsPackage.preferGlobal();

    // assert
    assertEquals( "Should return true", expectedPreferGlobal, actualPreferGlobal );
  }

//  @Test
//  public void testInitFromPackageJsonPathsCase() {
//    // arrange
//    Map<String, String> expectedPath = new HashMap<>();
//    expectedPath.put( "SomeModule", "/" );
//    Map<String, Object> preferGlobalJson = new HashMap<>();
//    preferGlobalJson.put( "paths", expectedPath );
//    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( preferGlobalJson );
//    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );
//
//    // act
//    boolean actualPreferGlobal = this.requireJsPackage.preferGlobal();
//
//    // assert
//    assertEquals( "Should return true", expectedPath, actualPreferGlobal );
//  }

  @Test
  public void testGetModulesWhenPackageDefinitionIsString() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    List<String> packages = new ArrayList<>(  );
    packages.add( "SomePackageName" );

    Map<String, Object> packagesJson = new HashMap<>();
    packagesJson.put( "packages", packages);
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packagesJson );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();

    // assert
    assertTrue( "Should contain package", actualModules.containsKey( expectedPackageName ) );
  }

//  @Test
//  public void testGetModulesWhenPackageDefinitionIsHashMap() {
//    // arrange
//    String expectedPackageName = "SomePackageName";
//    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );
//
//    Map<String, String> packages = new HashMap<>(  );
//    packages.put( "name", "SomeName" );
//    packages.put( "location", "SomeLocation" );
//
//    Map<String, Object> packagesJson = new HashMap<>();
//    packagesJson.put( "packages", packages);
//    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packagesJson );
//    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );
//
//    // act
//    Map<String, String> actualModules = this.requireJsPackage.getModules();
//
//    // assert
//    assertTrue( "Should contain package", actualModules.containsKey( expectedPackageName ) );
//  }
//
//  @Test
//  public void testGetModuleMainFile() {
//    // arrange
//
//    // act
//
//    // assert
//  }

  @Test
  public void testGetDependencies() {
    // arrange
    Map<String, String> expectedDependencies = new HashMap<>();
    expectedDependencies.put( "a", "1.1.1" );
    expectedDependencies.put( "b", "2.2.2" );
    Map<String, Object> dependencies = new HashMap<>();
    dependencies.put( "dependencies", expectedDependencies );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( dependencies );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, String> actualDependencies = this.requireJsPackage.getDependencies();

    // assert
    assertEquals( expectedDependencies, actualDependencies );
  }

  @Test
  public void testHasScript() {
    // arrange
    Map<String, String> expectedScripts = new HashMap<>();
    expectedScripts.put( "a", "a.js" );
    expectedScripts.put( "b", "b.js" );
    Map<String, Object> scripts = new HashMap<>();
    scripts.put( "scripts", expectedScripts );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( scripts );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );
    String scriptThatShouldExist = "a";

    // act
    boolean hasScript = this.requireJsPackage.hasScript( scriptThatShouldExist );

    // assert
    assertTrue( hasScript );
  }

  @Test
  public void testGetScriptResource() {
    // arrange
    String scriptName = "a";
    when( mockPentahoWebPackage.getResourceRootPath() ).thenReturn( "some/resource/path" );
    Map<String, String> expectedScripts = new HashMap<>();
    expectedScripts.put( scriptName, "a.js" );
    Map<String, Object> scripts = new HashMap<>();
    scripts.put( "scripts", expectedScripts );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( scripts );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    URL mockUrl = this.createMockUrlConnection( "" );
    when( mockBundle.getResource( any( String.class ) ) ).thenReturn( mockUrl );

    // make sure script exists
    assertTrue( this.requireJsPackage.hasScript( scriptName ) );

    // act
    URL actualScriptResource = this.requireJsPackage.getScriptResource( scriptName );

    // assert
    assertNotNull( actualScriptResource );
  }


  @Test
  public void testGetConfig() {
    // arrange
    String configName = "someConfig";
    Map<String, Map<String, String>> expectedConfigs = new HashMap<>();
    Map<String, String> configuration = new HashMap<>();
    expectedConfigs.put( configName, configuration );
    Map<String, Object> configs = new HashMap<>();
    configs.put( "config", expectedConfigs );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( configs );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, Map<String, ?>> actualConfigs = this.requireJsPackage.getConfig();

    // assert
    assertTrue( actualConfigs.containsKey( configName ) );
  }


  @Test
  public void testGetMap() {
    // arrange
    String mapName = "someMap";
    Map<String, Map<String, String>> expectedMap = new HashMap<>();
    Map<String, String> map = new HashMap<>();
    map.put( "aMapEntry", "path/to/something" );
    expectedMap.put( mapName, map );
    Map<String, Object> configs = new HashMap<>();
    configs.put( "map", expectedMap );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( configs );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, Map<String, String>> actualMap = this.requireJsPackage.getMap();

    // assert
    assertTrue( "Should have the map", actualMap.containsKey( mapName ) );
  }


  @Test
  public void testGetShimWhenConfigurationIsMap() {
    // arrange
    String shimName = "someShim";
    Map<String, Map<String, String>> expectedShim = new HashMap<>();
    Map<String, String> shim = new HashMap<>();
    shim.put( "aShimEntry", "path/to/something" );
    expectedShim.put( shimName, shim );
    Map<String, Object> shims = new HashMap<>();
    shims.put( "shim", expectedShim );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( shims );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, ?> actualShim = this.requireJsPackage.getShim();

    // assert
    assertTrue( actualShim.containsKey( shimName ) );
  }

  @Test
  public void testGetShimWhenConfigurationIsList() {
    // arrange
    String shimName = "someShim";
    Map<String, List<String>> expectedShim = new HashMap<>();
    List<String> shim = new ArrayList<>();
    shim.add( "Some shim data" );
    expectedShim.put( shimName, shim );
    Map<String, Object> shims = new HashMap<>();
    shims.put( "shim", expectedShim );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( shims );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    Map<String, ?> actualShim = this.requireJsPackage.getShim();

    // assert
    assertTrue( actualShim.containsKey( shimName ) );
  }


  @Test
  public void register() {
    // arrange
    ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
    when( this.mockBundleContext.registerService( anyString(), anyObject(), eq( null ) ) )
        .thenReturn( mockServiceRegistration );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );

    // act
    this.requireJsPackage.register();

    // assert
    verify( this.mockBundleContext, times( 1 ) )
        .registerService( anyString(), anyObject(), eq( null ) );
  }


  @Test
  public void unregister() {
    // arrange
    ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
    when( this.mockBundleContext.registerService( anyString(), anyObject(), eq( null ) ) )
        .thenReturn( mockServiceRegistration );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockBundleContext, this.mockPentahoWebPackage );
    this.requireJsPackage.register();

    // act
    this.requireJsPackage.register();
    this.requireJsPackage.unregister();

    // assert
    verify( mockServiceRegistration, times( 1 ) ).unregister();
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