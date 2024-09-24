/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequireJsPackageImplTest {

  private RequireJsPackageImpl requireJsPackage;

  private IPentahoWebPackage mockPentahoWebPackage;
  private URI resourceRootUri;

  @Before
  public void setUp() throws URISyntaxException {
    mockPentahoWebPackage = mock( IPentahoWebPackage.class );
    resourceRootUri = this.getClass().getResource( "/" ).toURI();
  }

  @Test
  public void testGetName() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    boolean actualPreferGlobal = this.requireJsPackage.preferGlobal();

    // assert
    assertEquals( "Should return true", expectedPreferGlobal, actualPreferGlobal );
  }

  @Test
  public void testDefaultModuleNoMain() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertNull( "Default module should have no main file", main );
  }

  @Test
  public void testDefaultModuleWithMainField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "main", "./some-path/some-file.js" );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertEquals( "Default module should have main file with no .js extension", "some-path/some-file", main );
  }

  @Test
  public void testDefaultModuleWithArrayMainField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    List<String> mainArray = new ArrayList<>();
    mainArray.add( "./some-path/some-style.css" );
    mainArray.add( "./some-path/some-code.js" );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "main", mainArray );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertEquals( "Default module should have main file with no .js extension", "some-path/some-code", main );
  }

  @Test
  public void testDefaultModuleWithSimpleBrowserField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "browser", "./some-path/some-file.js" );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertEquals( "Default module should have main file with no .js extension", "some-path/some-file", main );
  }

  @Test
  public void testDefaultModuleWithAdvancedBrowserField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    Map<String, Object> browser = new HashMap<>();
    browser.put( "./to-replace/file.js", "./some-path/some-file.js" );
    browser.put( "other-module", "./my-internal/version-of-module.js" );
    browser.put( "./to-ignore/file.js", false );
    browser.put( "to-ignore-module", false );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "browser", browser );

    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );
    Map<String, Map<String, String>> actualMappings = this.requireJsPackage.getMap();

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertNull( "Default module should have no main file", main );

    assertEquals( "Module for replaced file should be created", "/some-path/some-file", actualModules.get( expectedPackageName + "/to-replace/file" ) );

    assertEquals( "Mapping for replaced module should be created", expectedPackageName + "/my-internal/version-of-module", actualMappings.get( expectedPackageName ).get( "other-module" ) );

    assertEquals( "Mapping for ignored file should be created", "no-where-to-be-found", actualMappings.get( expectedPackageName ).get( expectedPackageName + "/to-ignore/file" ) );
    assertEquals( "Mapping for ignored module should be created", "no-where-to-be-found", actualMappings.get( expectedPackageName ).get( "to-ignore-module" ) );
  }

  @Test
  public void testDefaultModuleWithUnpkgField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "unpkg", "/some-path/some-file.js" );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertEquals( "Default module should have main file with no .js extension", "some-path/some-file", main );
  }

  @Test
  public void testDefaultModuleWithJsdelivrField() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );

    Map<String, Object> packageJson = new HashMap<>();
    packageJson.put( "jsdelivr", "some-path/some-file.js" );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packageJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();
    String main = this.requireJsPackage.getModuleMainFile( expectedPackageName );

    // assert
    assertEquals( "Default module should be created", "/", actualModules.get( expectedPackageName ) );
    assertEquals( "Default module should have main file with no .js extension", "some-path/some-file", main );
  }

  @Test
  public void testInitFromPackageJsonPathsCase() {
    // arrange
    String expectedPackageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( expectedPackageName );
    String moduleName = "SomeModule";
    Map<String, String> expectedPath = new HashMap<>();
    expectedPath.put( moduleName, "/" );
    Map<String, Object> pathsJson = new HashMap<>();
    pathsJson.put( "paths", expectedPath );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( pathsJson );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();

    // assert
    assertTrue( "SomeModule should exist in Modules collection",
        actualModules.containsKey( moduleName ) );
    assertFalse( "Default module should not be created",
        actualModules.containsKey( expectedPackageName ) );
  }

  @Test
  public void testGetModulesWhenPackageDefinitionIsString() {
    // arrange
    String expectedPackageName = "SomePackageName";
    List<String> packages = new ArrayList<>();
    packages.add( expectedPackageName );

    Map<String, Object> packagesJson = new HashMap<>();
    packagesJson.put( "packages", packages );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packagesJson );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();

    // assert
    assertTrue( "Should contain package", actualModules.containsKey( expectedPackageName ) );
  }

  @Test
  public void testGetModulesWhenPackageDefinitionIsHashMap() {
    // arrange
    Map<String, String> packageDefinition = new HashMap<>();
    packageDefinition.put( "name", "a name" );
    packageDefinition.put( "location", "a location" );

    List<Map<String, String>> packages = new ArrayList<>();
    packages.add( packageDefinition );

    Map<String, Object> packagesJson = new HashMap<>();
    packagesJson.put( "packages", packages );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packagesJson );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, String> actualModules = this.requireJsPackage.getModules();

    // assert
    assertTrue( "Should contain module", actualModules.containsKey( "a name" ) );
  }

  @Test
  public void testGetModuleMainFile() {
    // arrange
    String expectedModuleName = "main";
    String packageName = "SomePackageName";
    when( mockPentahoWebPackage.getName() ).thenReturn( packageName );

    List<String> packages = new ArrayList<>();
    packages.add( packageName );

    Map<String, Object> packagesJson = new HashMap<>();
    packagesJson.put( "packages", packages );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( packagesJson );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    String actualModuleName = this.requireJsPackage.getModuleMainFile( packageName );

    // assert
    assertEquals( "Module names should be equal", expectedModuleName, actualModuleName );
  }

  @Test
  public void testGetDependencies() {
    // arrange
    Map<String, String> expectedDependencies = new HashMap<>();
    expectedDependencies.put( "a", "1.1.1" );
    expectedDependencies.put( "b", "2.2.2" );
    Map<String, Object> dependencies = new HashMap<>();
    dependencies.put( "dependencies", expectedDependencies );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( dependencies );
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );
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

    Map<String, String> expectedScripts = new HashMap<>();
    expectedScripts.put( scriptName, "a.js" );
    Map<String, Object> scripts = new HashMap<>();
    scripts.put( "scripts", expectedScripts );
    when( mockPentahoWebPackage.getPackageJson() ).thenReturn( scripts );

    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // make sure script exists
    assertTrue( this.requireJsPackage.hasScript( scriptName ) );

    // act
    URL actualScriptResource = this.requireJsPackage.getScriptResource( scriptName );

    // assert
    assertNotNull( actualScriptResource );
    assertTrue( actualScriptResource.toExternalForm().endsWith( "/a.js" ) );
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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

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
    this.requireJsPackage = new RequireJsPackageImpl( this.mockPentahoWebPackage, this.resourceRootUri );

    // act
    Map<String, ?> actualShim = this.requireJsPackage.getShim();

    // assert
    assertTrue( actualShim.containsKey( shimName ) );
  }

}
