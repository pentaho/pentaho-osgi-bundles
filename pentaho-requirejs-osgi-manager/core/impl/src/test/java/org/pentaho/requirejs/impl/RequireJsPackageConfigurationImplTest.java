/*!
 * Copyright 2018 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RequireJsPackageConfigurationImplTest {

  private Map<String, String> expectedModuleIdsMapping;

  private Map<String, String> modules;
  private Map<String, String> dependencies;
  private String name;
  private String version;
  private String webRoot;
  private HashMap<Object, Object> dependencyABaseModuleIdsMapping;
  private HashMap<Object, Object> dependencyCBaseModuleIdsMapping;

  @Before
  public void setUp() {
    name = "@tests/basic";
    version = "1.0.1";
    webRoot = "web/path";

    modules = new HashMap<>();
    modules.put( "some/module/A", "/some/path/to/module/A" );
    modules.put( "some/module/B", "/other/path/to/module/B" );
    modules.put( "at-root", "/the/root/one" );

    dependencies = new HashMap<>();
    dependencies.put( "@dep/A", "1.0" );
    dependencies.put( "@dep/B", "1.5" );
    dependencies.put( "@dep/C", "2.5" );

    dependencyABaseModuleIdsMapping = new HashMap<>();
    dependencyABaseModuleIdsMapping.put( "depA", "depA@1.0" );
    dependencyABaseModuleIdsMapping.put( "depA/hi", "depA@1.0/depA/hi" );
    dependencyABaseModuleIdsMapping.put( "depA/hello", "depA@1.0/depA/hello" );

    dependencyCBaseModuleIdsMapping = new HashMap<>();
    dependencyCBaseModuleIdsMapping.put( "depC/hi", "depC@1.5/depC/hi" );
    dependencyCBaseModuleIdsMapping.put( "depC/hello", "depC@1.5/depC/hello" );

    expectedModuleIdsMapping = new HashMap<>();
    expectedModuleIdsMapping.put( "some/module/A", name + "@" + version + "/" + "some/module/A" );
    expectedModuleIdsMapping.put( "some/module/B", name + "@" + version + "/" + "some/module/B" );
    expectedModuleIdsMapping.put( "at-root", name + "@" + version + "/" + "at-root" );
    expectedModuleIdsMapping.put( "depA", "depA@1.0/depA" );
    expectedModuleIdsMapping.put( "depA/hi", "depA@1.0/depA/hi" );
    expectedModuleIdsMapping.put( "depA/hello", "depA@1.0/depA/hello" );
    expectedModuleIdsMapping.put( "depC/hi", "depC@1.5/depC/hi" );
    expectedModuleIdsMapping.put( "depC/hello", "depC@1.5/depC/hello" );
  }

  @Test(expected = IllegalArgumentException.class)
  public void requireJsPackageIsRequired() {
    new RequireJsPackageConfigurationImpl( null );
  }

  @Test
  public void getBaseModuleIdsMappingEmpty() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertNotNull( "Should always return a base module IDs mapping", baseModuleIdsMapping );
    assertTrue( "Should return an empty base module IDs mapping", baseModuleIdsMapping.isEmpty() );
  }

  @Test
  public void getBaseModuleIdsMapping() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    // processRequireJsPackage is called at construction time, so the base modules IDs of all packages are available during dependency resolution
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertEquals( "Only contains one mapping for each module", modules.size(), baseModuleIdsMapping.size() );
    modules.forEach( ( moduleId, path ) -> assertTrue( "Maps a versioned ID to each module", baseModuleIdsMapping.containsKey( moduleId ) ) );
  }

  @Test
  public void getBaseModuleIdsMappingGlobalPackage() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );
    doReturn( true ).when( mockRequireJsPackage ).preferGlobal();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertEquals( "Should not contain any mappings for its module", 0, baseModuleIdsMapping.size() );
  }

  @Test
  public void getBaseModuleIdsMappingModuleIdsWithOrganization() {
    String name = "@tests/basic";
    String version = "1.0";

    Map<String, String> modules = new HashMap<>();
    modules.put( "basic/A", "/" );
    modules.put( "tests/basic/A", "/" );
    modules.put( "other/A", "/" );
    modules.put( "tests/other/A", "/" );

    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    // @test/basic@1.0/basic/A/
    assertEquals( "@tests/basic@1.0/basic/A", baseModuleIdsMapping.get( "basic/A" ) );
    assertEquals( "@tests/basic@1.0/other/A", baseModuleIdsMapping.get( "other/A" ) );

    assertEquals( "@tests/basic@1.0/tests/basic/A", baseModuleIdsMapping.get( "tests/basic/A" ) );
    assertEquals( "@tests/basic@1.0/tests/other/A", baseModuleIdsMapping.get( "tests/other/A" ) );
  }

  @Test
  public void getBaseModuleIdsMappingModuleIdsWithOrganizationAndStructure() {
    String name = "@tests/basic-stuff";
    String version = "1.0";

    Map<String, String> modules = new HashMap<>();
    modules.put( "tests/basic/stuff/A", "/" );
    modules.put( "basic/stuff/A", "/" );

    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    assertEquals( "@tests/basic-stuff@1.0/tests/basic/stuff/A", baseModuleIdsMapping.get( "tests/basic/stuff/A" ) );
    assertEquals( "@tests/basic-stuff@1.0/basic/stuff/A", baseModuleIdsMapping.get( "basic/stuff/A" ) );
  }

  @Test
  public void getBaseModuleIdsMappingModuleIdsWithoutOrganization() {
    String name = "basic";
    String version = "1.0";

    Map<String, String> modules = new HashMap<>();
    modules.put( "basic/A", "/" );
    modules.put( "other/A", "/" );

    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    assertEquals( "basic@1.0/basic/A", baseModuleIdsMapping.get( "basic/A" ) );
    assertEquals( "basic@1.0/other/A", baseModuleIdsMapping.get( "other/A" ) );
  }

  @Test
  public void getBaseModuleIdsMappingModuleIdsWithoutOrganizationButStructure() {
    String name = "tests-basic";
    String version = "1.0";

    Map<String, String> modules = new HashMap<>();
    modules.put( "tests/basic/A", "/" );
    modules.put( "tests/other/A", "/" );
    modules.put( "basic/A", "/" );
    modules.put( "other/A", "/" );

    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    assertEquals( "tests-basic@1.0/tests/basic/A", baseModuleIdsMapping.get( "tests/basic/A" ) );
    assertEquals( "tests-basic@1.0/tests/other/A", baseModuleIdsMapping.get( "tests/other/A" ) );

    assertEquals( "tests-basic@1.0/basic/A", baseModuleIdsMapping.get( "basic/A" ) );
    assertEquals( "tests-basic@1.0/other/A", baseModuleIdsMapping.get( "other/A" ) );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getBaseModuleIdsMappingReturnsUnmodifiableMap() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    baseModuleIdsMapping.put( "moduleId", "mappedId" );
  }

  @Test
  public void processRequireJsPackage() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    // processRequireJsPackage is called at construction time, so the base modules IDs of all packages are available during dependency resolution
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    HashMap<Object, Object> newSetOfModules = new HashMap<>();
    newSetOfModules.put( "other/module/C", "/some/path/to/module/C" );
    newSetOfModules.put( "other/module/D", "/other/path/to/module/D" );

    doReturn( newSetOfModules ).when( mockRequireJsPackage ).getModules();

    assertEquals( "baseModuleIdsMapping shouldn't change", baseModuleIdsMapping, packageConfiguration.getBaseModuleIdsMapping() );

    packageConfiguration.processRequireJsPackage();

    Map<String, String> otherBaseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertNotEquals( "baseModuleIdsMapping should change", baseModuleIdsMapping, otherBaseModuleIdsMapping );
    assertEquals( "Only contains one mapping for each module", newSetOfModules.size(), otherBaseModuleIdsMapping.size() );
    newSetOfModules.forEach( ( moduleId, path ) -> assertTrue( "Maps a versioned ID to each new module", otherBaseModuleIdsMapping.containsKey( moduleId ) ) );

    doReturn( true ).when( mockRequireJsPackage ).preferGlobal();

    assertEquals( "baseModuleIdsMapping shouldn't change", otherBaseModuleIdsMapping, packageConfiguration.getBaseModuleIdsMapping() );

    packageConfiguration.processRequireJsPackage();

    Map<String, String> yetAnotherBaseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertNotEquals( "baseModuleIdsMapping should change", otherBaseModuleIdsMapping, yetAnotherBaseModuleIdsMapping );
    assertEquals( "Should not contain any mappings for its module", 0, yetAnotherBaseModuleIdsMapping.size() );
  }

  @Test
  public void processDependencies() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

    // Should call the dependencyResolverFunction for each dependency
    verify( dependencyResolverFunction, times( dependencies.size() ) ).apply( anyString(), anyString() );
  }

  @Test(expected = IllegalStateException.class)
  public void getModuleIdsMappingBeforeProcessDependencies() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.getModuleIdsMapping();
  }

  @Test(expected = IllegalStateException.class)
  public void processRequireJsPackageInvalidatesProcessDependencies() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );

    packageConfiguration.processDependencies( dependencyResolverFunction );

    packageConfiguration.getModuleIdsMapping();

    packageConfiguration.processRequireJsPackage();

    packageConfiguration.getModuleIdsMapping();
  }

  @Test
  public void getModuleIdsMapping() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> moduleIdsMapping = packageConfiguration.getModuleIdsMapping();

    assertEquals( "Should return mappings with its own base moduleIds and of its resolved dependencies (all but @dep/B)", modules.size() + dependencyABaseModuleIdsMapping.size() + dependencyCBaseModuleIdsMapping.size(), moduleIdsMapping.size() );

    modules.forEach( ( moduleId, path ) -> {
      assertTrue( "Maps a versioned ID to each module", moduleIdsMapping.containsKey( moduleId ) );
    } );

    dependencyABaseModuleIdsMapping.forEach( ( moduleId, mappedModuleId ) -> {
      assertTrue( "Maps a versioned ID to each dependency module", moduleIdsMapping.containsKey( moduleId ) );
      assertEquals( "Maps a versioned ID to each dependency module", moduleIdsMapping.get( moduleId ), mappedModuleId );
    } );

    dependencyCBaseModuleIdsMapping.forEach( ( moduleId, mappedModuleId ) -> {
      assertTrue( "Maps a versioned ID to each dependency module", moduleIdsMapping.containsKey( moduleId ) );
      assertEquals( "Maps a versioned ID to each dependency module", moduleIdsMapping.get( moduleId ), mappedModuleId );
    } );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getModuleIdsMappingReturnsUnmodifiableMap() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> moduleIdsMapping = packageConfiguration.getModuleIdsMapping();
    moduleIdsMapping.put( "moduleId", "mappedId" );
  }

  @Test
  public void getEmptyRequireConfig() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    assertTrue( "Should have empty paths map", ( (Map<String, ?>) requireConfig.get( "paths" ) ).isEmpty() );
    assertTrue( "Should have empty map map", ( (Map<String, ?>) requireConfig.get( "map" ) ).isEmpty() );
    assertTrue( "Should have empty packages list", ( (List<?>) requireConfig.get( "packages" ) ).isEmpty() );
    assertTrue( "Should have empty config map", ( (Map<String, ?>) requireConfig.get( "config" ) ).isEmpty() );
    assertTrue( "Should have empty shim map", ( (Map<String, ?>) requireConfig.get( "shim" ) ).isEmpty() );
  }

  @Test
  public void getRequireConfigPaths() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );

    modules.forEach( ( moduleId, path ) -> {
      String versionedModuleID = baseModuleIdsMapping.get( moduleId );

      assertTrue( "Defines path for the versioned module ID", paths.containsKey( versionedModuleID ) );

      String versionedPath = paths.get( versionedModuleID );
      assertEquals( "Module ID path is prepended with web root path", webRoot + path, versionedPath );
    } );
  }

  @Test
  public void getRequireConfigPathsRootPath() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot );

    Map<String, String> modules = new HashMap<>();
    modules.put( "tests/basic/A", "/" );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );

    String versionedPath = paths.get( baseModuleIdsMapping.get( "tests/basic/A" ) );
    assertEquals( "Module ID path is the web root path", webRoot, versionedPath );
  }

  @Test
  public void getRequireConfigPathsWithoutPathBeginningSlash() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot );

    Map<String, String> modules = new HashMap<>();
    modules.put( "tests/basic/A", "something/without/beginning-slash" );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );

    String versionedPath = paths.get( baseModuleIdsMapping.get( "tests/basic/A" ) );
    assertEquals( "Module ID path is prepended with web root path and a slash", webRoot + "/something/without/beginning-slash", versionedPath );
  }

  @Test
  public void getRequireConfigPackages() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    List<?> packages = (List<?>) requireConfig.get( "packages" );

    assertTrue( "Should have package in string format for some/module/B (main)", packages.contains( baseModuleIdsMapping.get( "some/module/B" ) ) );

    Map<String, String> packageDefinition = new HashMap<>( 2 );
    packageDefinition.put( "name", baseModuleIdsMapping.get( "at-root" ) );
    packageDefinition.put( "main", "special" );
    assertTrue( "Should have package in object format for at-root (special)", packages.contains( packageDefinition ) );

    assertTrue( "Should not have any other package (some/module/A is not a package)", packages.size() == 2 );
  }

  @Test
  public void getRequireConfigMaps() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) requireConfig.get( "map" );

    modules.forEach( ( moduleId, path ) -> {
      String versionedModuleID = baseModuleIdsMapping.get( moduleId );

      assertTrue( "Creates mappings for the versioned module ID", map.containsKey( versionedModuleID ) );

      Map<String, String> moduleMap = map.get( versionedModuleID );

      modules.keySet().forEach( ( internalModuleId ) -> assertTrue( "Maps the package's modules to each other", moduleMap.containsKey( internalModuleId ) ) );
    } );
  }

  @Test
  public void getRequireConfigMapsPackageMapsMerge() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    doReturn( modules ).when( mockRequireJsPackage ).getModules();

    Map<String, Map<String, String>> packageMaps = new HashMap<>();
    Map<String, String> baseModulePackageMap = new HashMap<>();
    baseModulePackageMap.put( "B", "some/module/B" );
    baseModulePackageMap.put( "Hello", "depA/hello" );
    baseModulePackageMap.put( "Hi", "depC/hi" );
    baseModulePackageMap.put( "angular", "any-other-module-id" );
    packageMaps.put( "some/module/A", baseModulePackageMap );

    Map<String, String> dependencyModulePackageMap = new HashMap<>();
    dependencyModulePackageMap.put( "B", "some/module/B" );
    dependencyModulePackageMap.put( "Hello", "depA/hello" );
    dependencyModulePackageMap.put( "Hi", "depC/hi" );
    dependencyModulePackageMap.put( "angular", "any-other-module-id" );
    packageMaps.put( "depA/hi", dependencyModulePackageMap );

    Map<String, String> otherModulePackageMap = new HashMap<>();
    otherModulePackageMap.put( "B", "some/module/B" );
    otherModulePackageMap.put( "Hello", "depA/hello" );
    otherModulePackageMap.put( "Hi", "depC/hi" );
    otherModulePackageMap.put( "angular", "any-other-module-id" );
    packageMaps.put( "tests/complex/other", otherModulePackageMap );

    doReturn( packageMaps ).when( mockRequireJsPackage ).getMap();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) requireConfig.get( "map" );

    assertTrue( "Doesn't create mappings for the unversioned base module ID", !map.containsKey( "some/module/A" ) );
    assertTrue( "Creates mappings for the versioned base module ID", map.containsKey( expectedModuleIdsMapping.get( "some/module/A" ) ) );

    Map<String, String> baseModuleMap = map.get( expectedModuleIdsMapping.get( "some/module/A" ) );
    baseModulePackageMap.keySet().forEach( ( internalModuleId ) -> assertTrue( "The package map was merged (" + internalModuleId + ")", baseModuleMap.containsKey( internalModuleId ) ) );
    assertEquals( "Mapped base module ID is translated", baseModuleMap.get( "B" ), expectedModuleIdsMapping.get( "some/module/B" ) );
    assertEquals( "Mapped dependency module ID is translated", baseModuleMap.get( "Hello" ), expectedModuleIdsMapping.get( "depA/hello" ) );
    assertEquals( "Mapped dependency module ID is translated", baseModuleMap.get( "Hi" ), expectedModuleIdsMapping.get( "depC/hi" ) );
    assertEquals( "Mapped other module ID is not translated", baseModuleMap.get( "angular" ), "any-other-module-id" );

    assertTrue( "Creates create mappings for the unversioned dependency module ID", map.containsKey( "depA/hi" ) );
    assertTrue( "Doesn't mappings for the versioned dependency module ID", !map.containsKey( expectedModuleIdsMapping.get( "depA/hi" ) ) );

    Map<String, String> dependencyModuleMap = map.get( "depA/hi" );
    dependencyModulePackageMap.keySet().forEach( ( internalModuleId ) -> assertTrue( "The package map was merged (" + internalModuleId + ")", dependencyModuleMap.containsKey( internalModuleId ) ) );
    assertEquals( "Mapped base module ID is translated", dependencyModuleMap.get( "B" ), expectedModuleIdsMapping.get( "some/module/B" ) );
    assertEquals( "Mapped dependency module ID is translated", dependencyModuleMap.get( "Hello" ), expectedModuleIdsMapping.get( "depA/hello" ) );
    assertEquals( "Mapped dependency module ID is translated", dependencyModuleMap.get( "Hi" ), expectedModuleIdsMapping.get( "depC/hi" ) );
    assertEquals( "Mapped other module ID is not translated", dependencyModuleMap.get( "angular" ), "any-other-module-id" );

    assertTrue( "Creates mappings for the external module ID", map.containsKey( "tests/complex/other" ) );

    Map<String, String> otherModuleMap = map.get( "tests/complex/other" );
    otherModulePackageMap.keySet().forEach( ( internalModuleId ) -> assertTrue( "The package map was merged (" + internalModuleId + ")", otherModuleMap.containsKey( internalModuleId ) ) );
    assertEquals( "Mapped base module ID is translated", otherModuleMap.get( "B" ), expectedModuleIdsMapping.get( "some/module/B" ) );
    assertEquals( "Mapped dependency module ID is translated", otherModuleMap.get( "Hello" ), expectedModuleIdsMapping.get( "depA/hello" ) );
    assertEquals( "Mapped dependency module ID is translated", otherModuleMap.get( "Hi" ), expectedModuleIdsMapping.get( "depC/hi" ) );
    assertEquals( "Mapped other module ID is not translated", otherModuleMap.get( "angular" ), "any-other-module-id" );
  }

  @Test
  public void getRequireConfigConfig() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    Map<String, Map<String, String>> packageConfig = new HashMap<>();

    Map<String, String> configOfPackageModule = new HashMap<>();
    configOfPackageModule.put( "just", "1" );
    packageConfig.put( "some/module/A", configOfPackageModule );

    Map<String, String> configOfDependencyModule = new HashMap<>();
    configOfDependencyModule.put( "just", "2" );
    packageConfig.put( "depA/hi", configOfDependencyModule );

    Map<String, String> configOfOtherModule = new HashMap<>();
    configOfOtherModule.put( "just", "3" );
    packageConfig.put( "tests/complex/other", configOfOtherModule );

    doReturn( packageConfig ).when( mockRequireJsPackage ).getConfig();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );

    assertTrue( "There isn't a config for the unversioned base module ID", !config.containsKey( "some/module/A" ) );
    assertTrue( "There is a config for the versioned base module ID", config.containsKey( expectedModuleIdsMapping.get( "some/module/A" ) ) );

    assertTrue( "There isn't a config for the unversioned dependency module ID", !config.containsKey( "depA/hi" ) );
    assertTrue( "There is a config for the versioned dependency module ID", config.containsKey( expectedModuleIdsMapping.get( "depA/hi" ) ) );

    assertTrue( "There is a config for the other module ID", config.containsKey( "tests/complex/other" ) );
  }

  @Test
  public void getRequireConfigShim() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    Map<String, Map<String, ?>> packageShim = new HashMap<>();

    Map<String, Object> shimOfPackageModule = new HashMap<>();
    List<String> deps = new ArrayList<>();
    deps.add( "some/module/B" );
    deps.add( "depA/hello" );
    deps.add( "jquery" );
    shimOfPackageModule.put( "deps", deps );
    packageShim.put( "some/module/A", shimOfPackageModule );

    Map<String, String> shimOfDependencyModule = new HashMap<>();
    shimOfDependencyModule.put( "exports", "hi" );
    packageShim.put( "depA/hi", shimOfDependencyModule );

    Map<String, String> configOfOtherModule = new HashMap<>();
    configOfOtherModule.put( "exports", "other" );
    packageShim.put( "tests/complex/other", configOfOtherModule );

    doReturn( packageShim ).when( mockRequireJsPackage ).getShim();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, ?>> shim = (Map<String, Map<String, ?>>) requireConfig.get( "shim" );

    assertTrue( "There isn't a shim config for the unversioned base module ID", !shim.containsKey( "some/module/A" ) );
    assertTrue( "There is a shim config for the versioned base module ID", shim.containsKey( expectedModuleIdsMapping.get( "some/module/A" ) ) );

    List<String> shimADeps = (List<String>) shim.get( expectedModuleIdsMapping.get( "some/module/A" ) ).get( "deps" );
    {
      assertTrue( "There isn't a dep for the unversioned base module ID", !shimADeps.contains( "some/module/B" ) );
      assertTrue( "There is a dep for the versioned base module ID", shimADeps.contains( expectedModuleIdsMapping.get( "some/module/B" ) ) );

      assertTrue( "There isn't a dep for the unversioned dependency module ID", !shimADeps.contains( "depA/hello" ) );
      assertTrue( "There is a dep for the versioned dependency module ID", shimADeps.contains( expectedModuleIdsMapping.get( "depA/hello" ) ) );

      assertTrue( "There is a dep for the other module ID", shimADeps.contains( "jquery" ) );
    }

    assertTrue( "There is a shim config for the unversioned dependency module ID", shim.containsKey( "depA/hi" ) );

    assertTrue( "There is a shim config for the other module ID", shim.containsKey( "tests/complex/other" ) );
  }

  @Test
  public void getRequireConfigPluginsAreCalled() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mock( IRequireJsPackageConfigurationPlugin.class ) );
    plugins.add( mock( IRequireJsPackageConfigurationPlugin.class ) );
    plugins.add( mock( IRequireJsPackageConfigurationPlugin.class ) );

    packageConfiguration.getRequireConfig( plugins );

    plugins.forEach( plugin -> verify( plugin, times( 1 ) ).apply( same( packageConfiguration ), any(), any(), any() ) );
  }

  @Test
  public void getRequireConfigPluginsThrowsAreIgnored() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mock( IRequireJsPackageConfigurationPlugin.class ) );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doThrow( new RuntimeException() ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    plugins.add( mockPlugin );

    plugins.add( mock( IRequireJsPackageConfigurationPlugin.class ) );

    packageConfiguration.getRequireConfig( plugins );

    plugins.forEach( plugin -> verify( plugin, times( 1 ) ).apply( same( packageConfiguration ), any(), any(), any() ) );
  }

  @Test
  public void getRequireConfigPluginCanModifyConfigAndShim() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();
      Map<String, ?> requireConfig = (Map<String, ?>) args[ 3 ];

      Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );
      config.put( "my-key", new HashMap<>() );

      Map<String, Map<String, ?>> shim = (Map<String, Map<String, ?>>) requireConfig.get( "shim" );
      shim.put( "my-key", new HashMap<>() );

      return null;
    } ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mockPlugin );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( plugins );

    Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );
    assertTrue( "Config was modified", config.containsKey( "my-key" ) );

    Map<String, Map<String, ?>> shim = (Map<String, Map<String, ?>>) requireConfig.get( "shim" );
    assertTrue( "Shim was modified", shim.containsKey( "my-key" ) );
  }

  @Test
  public void getRequireConfigPluginCannotModifyTop() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();
      Map<String, Object> requireConfig = (Map<String, Object>) args[ 3 ];

      requireConfig.put( "my-key", "something" );

      return null;
    } ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mockPlugin );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( plugins );

    assertTrue( "Top was not modified", !requireConfig.containsKey( "my-key" ) );
  }

  @Test
  public void getRequireConfigPluginCannotModifyPaths() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();
      Map<String, ?> requireConfig = (Map<String, ?>) args[ 3 ];

      Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );
      paths.put( "my-key", "/" );

      return null;
    } ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mockPlugin );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( plugins );

    Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );
    assertTrue( "Paths was not modified", !paths.containsKey( "my-key" ) );
  }

  @Test
  public void getRequireConfigPluginCannotModifyPackages() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();
      Map<String, ?> requireConfig = (Map<String, ?>) args[ 3 ];

      List<Object> packages = (List<Object>) requireConfig.get( "packages" );
      packages.add( "my-key" );

      return null;
    } ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mockPlugin );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( plugins );

    List<Object> packages = (List<Object>) requireConfig.get( "packages" );
    assertTrue( "Packages was not modified", !packages.contains( "my-key" ) );
  }

  @Test
  public void getRequireConfigPluginCannotModifyMap() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    BiFunction<String, String, IRequireJsPackageConfiguration> mockDependencyResolverFunction = getMockDependencyResolverFunction();
    packageConfiguration.processDependencies( mockDependencyResolverFunction );

    IRequireJsPackageConfigurationPlugin mockPlugin = mock( IRequireJsPackageConfigurationPlugin.class );
    doAnswer( invocation -> {
      Object[] args = invocation.getArguments();
      Map<String, ?> requireConfig = (Map<String, ?>) args[ 3 ];

      Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) requireConfig.get( "map" );
      map.put( "my-key", new HashMap<>() );

      return null;
    } ).when( mockPlugin ).apply( same( packageConfiguration ), any(), any(), any() );

    List<IRequireJsPackageConfigurationPlugin> plugins = new ArrayList<>();
    plugins.add( mockPlugin );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( plugins );

    Map<String, Object> map = (Map<String, Object>) requireConfig.get( "map" );
    assertTrue( "Packages was not modified", !map.containsKey( "my-key" ) );
  }

  @Test
  public void testModuleIdVersioning() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules, dependencies );

    Map<String, Map<String, String>> packageConfig = new HashMap<>();

    packageConfig.put( "some/module/A", new HashMap<>() );
    packageConfig.put( "depA/hi", new HashMap<>() );
    packageConfig.put( "tests/complex/other", new HashMap<>() );

    packageConfig.put( "some/module/B!some/module/A", new HashMap<>() );
    packageConfig.put( "some/module/B!depA/hi", new HashMap<>() );
    packageConfig.put( "some/module/B!tests/complex/other", new HashMap<>() );

    packageConfig.put( "depA/hello!some/module/A", new HashMap<>() );
    packageConfig.put( "depA/hello!depA/hi", new HashMap<>() );
    packageConfig.put( "depA/hello!tests/complex/other", new HashMap<>() );

    packageConfig.put( "plugin!some/module/A", new HashMap<>() );
    packageConfig.put( "plugin!depA/hi", new HashMap<>() );
    packageConfig.put( "plugin!tests/complex/other", new HashMap<>() );

    packageConfig.put( "depA@1.0/hi", new HashMap<>() );

    doReturn( packageConfig ).when( mockRequireJsPackage ).getConfig();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    packageConfiguration.processDependencies( getMockDependencyResolverFunction() );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );

    assertTrue( "Base module ID translation", config.containsKey( expectedModuleIdsMapping.get( "some/module/A" ) ) );
    assertTrue( "Dependency module ID translation", config.containsKey( expectedModuleIdsMapping.get( "depA/hi" ) ) );
    assertTrue( "Other module ID not translated", config.containsKey( "tests/complex/other" ) );

    assertTrue( "Base module ID loader translated + Base module ID translation", config.containsKey( expectedModuleIdsMapping.get( "some/module/B" ) + "!" + expectedModuleIdsMapping.get( "some/module/A" ) ) );
    assertTrue( "Base module ID loader translated + Dependency module ID translation", config.containsKey( expectedModuleIdsMapping.get( "some/module/B" ) + "!" + expectedModuleIdsMapping.get( "depA/hi" ) ) );
    assertTrue( "Base module ID loader translated + Other module ID not translated", config.containsKey( expectedModuleIdsMapping.get( "some/module/B" ) + "!" + "tests/complex/other" ) );

    assertTrue( "Dependency module ID loader translated + Base module ID translation", config.containsKey( expectedModuleIdsMapping.get( "depA/hello" ) + "!" + expectedModuleIdsMapping.get( "some/module/A" ) ) );
    assertTrue( "Dependency module ID loader translated + Dependency module ID translation", config.containsKey( expectedModuleIdsMapping.get( "depA/hello" ) + "!" + expectedModuleIdsMapping.get( "depA/hi" ) ) );
    assertTrue( "Dependency module ID loader translated + Other module ID not translated", config.containsKey( expectedModuleIdsMapping.get( "depA/hello" ) + "!" + "tests/complex/other" ) );

    assertTrue( "Other module ID loader not translated + Base module ID translation", config.containsKey( "plugin!" + expectedModuleIdsMapping.get( "some/module/A" ) ) );
    assertTrue( "Other module ID loader not translated + Dependency module ID translation", config.containsKey( "plugin!" + expectedModuleIdsMapping.get( "depA/hi" ) ) );
    assertTrue( "Other module ID loader not translated + Other module ID not translated", config.containsKey( "plugin!tests/complex/other" ) );
  }

  // region Access to the underlying RequireJsPackage
  @Test
  public void getRequireJsPackage() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    assertSame( mockRequireJsPackage, packageConfiguration.getRequireJsPackage() );
  }

  @Test
  public void getName() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( name ).when( mockRequireJsPackage ).getName();
    assertEquals( "Should return RequireJsPackage name", name, packageConfiguration.getName() );

    doReturn( null ).when( mockRequireJsPackage ).getName();
    assertEquals( "Should return empty name if RequireJsPackage name is null", "", packageConfiguration.getName() );
  }

  @Test
  public void getVersion() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( version ).when( mockRequireJsPackage ).getVersion();
    assertEquals( "Should return RequireJsPackage version", version, packageConfiguration.getVersion() );

    doReturn( null ).when( mockRequireJsPackage ).getVersion();
    assertEquals( "Should return empty version if RequireJsPackage version is null", "", packageConfiguration.getVersion() );
  }

  @Test
  public void getWebRootPath() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( webRoot ).when( mockRequireJsPackage ).getWebRootPath();
    assertEquals( "Should return RequireJsPackage web root path", webRoot, packageConfiguration.getWebRootPath() );

    doReturn( "/" + webRoot ).when( mockRequireJsPackage ).getWebRootPath();
    assertEquals( "Should remove leading slashes", webRoot, packageConfiguration.getWebRootPath() );

    doReturn( "//" + webRoot ).when( mockRequireJsPackage ).getWebRootPath();
    assertEquals( "Should remove leading slashes", webRoot, packageConfiguration.getWebRootPath() );

    doReturn( "///" + webRoot ).when( mockRequireJsPackage ).getWebRootPath();
    assertEquals( "Should remove leading slashes", webRoot, packageConfiguration.getWebRootPath() );
  }

  @Test
  public void getDependencies() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( dependencies ).when( mockRequireJsPackage ).getDependencies();

    Map<String, String> packageConfigurationDependencies = packageConfiguration.getDependencies();
    // it's ok to rely on AbstractMap's equal implementation
    assertEquals( "Should return RequireJsPackage version", dependencies, packageConfigurationDependencies );

    doReturn( null ).when( mockRequireJsPackage ).getDependencies();
    packageConfigurationDependencies = packageConfiguration.getDependencies();
    assertTrue( "Should return empty map if RequireJsPackage dependencies is null", packageConfigurationDependencies.isEmpty() );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getDependenciesReturnsUnmodifiableMap() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( dependencies ).when( mockRequireJsPackage ).getDependencies();

    Map<String, String> packageConfigurationDependencies = packageConfiguration.getDependencies();
    packageConfigurationDependencies.put( "moduleC", "7.8" );
  }

  @Test
  public void hasScript() {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( false ).when( mockRequireJsPackage ).hasScript( anyString() );
    doReturn( true ).when( mockRequireJsPackage ).hasScript( "preconfig" );

    assertEquals( "Should return RequireJsPackage hasScript result", true, packageConfiguration.hasScript( "preconfig" ) );
    assertEquals( "Should return RequireJsPackage hasScript result", false, packageConfiguration.hasScript( "postconfig" ) );
  }

  @Test
  public void getScriptResource() throws MalformedURLException {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( null ).when( mockRequireJsPackage ).getScriptResource( anyString() );
    URL toBeReturned = new URL( "file://some/path" );
    doReturn( toBeReturned ).when( mockRequireJsPackage ).getScriptResource( "preconfig" );

    assertSame( "Should return RequireJsPackage getScriptResource result", toBeReturned, packageConfiguration.getScriptResource( "preconfig" ) );
    assertEquals( "Should return RequireJsPackage getScriptResource result", null, packageConfiguration.getScriptResource( "postconfig" ) );
  }
  // endregion

  // region Mock factory
  private IRequireJsPackage getRequireJsPackageMock( String name, String version, String webRoot, Map<String, String> modules, Map<String, String> dependencies ) {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot, modules );

    if ( dependencies != null ) {
      doReturn( dependencies ).when( mockRequireJsPackage ).getDependencies();
    }

    return mockRequireJsPackage;
  }

  private IRequireJsPackage getRequireJsPackageMock( String name, String version, String webRoot, Map<String, String> modules ) {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version, webRoot );

    if ( modules != null ) {
      doReturn( modules ).when( mockRequireJsPackage ).getModules();

      doReturn( "main" ).when( mockRequireJsPackage ).getModuleMainFile( "some/module/B" );
      doReturn( "special" ).when( mockRequireJsPackage ).getModuleMainFile( "at-root" );
    }

    return mockRequireJsPackage;
  }

  private IRequireJsPackage getRequireJsPackageMock( String name, String version, String webRoot ) {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name, version );

    doReturn( webRoot ).when( mockRequireJsPackage ).getWebRootPath();

    return mockRequireJsPackage;
  }

  private IRequireJsPackage getRequireJsPackageMock( String name, String version ) {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock( name );

    doReturn( version ).when( mockRequireJsPackage ).getVersion();

    return mockRequireJsPackage;
  }

  private IRequireJsPackage getRequireJsPackageMock( String name ) {
    IRequireJsPackage mockRequireJsPackage = getRequireJsPackageMock();

    doReturn( name ).when( mockRequireJsPackage ).getName();

    return mockRequireJsPackage;
  }

  private IRequireJsPackage getRequireJsPackageMock() {
    return Mockito.mock( IRequireJsPackage.class );
  }

  private BiFunction<String, String, IRequireJsPackageConfiguration> getMockDependencyResolverFunction() {
    BiFunction<String, String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );

    IRequireJsPackageConfiguration dependencyA = mock( IRequireJsPackageConfiguration.class );
    doReturn( dependencyABaseModuleIdsMapping ).when( dependencyA ).getBaseModuleIdsMapping();
    doReturn( dependencyA ).when( dependencyResolverFunction ).apply( eq( "@dep/A" ), anyString() );

    doReturn( null ).when( dependencyResolverFunction ).apply( eq( "@dep/B" ), anyString() );

    IRequireJsPackageConfiguration dependencyC = mock( IRequireJsPackageConfiguration.class );
    doReturn( dependencyCBaseModuleIdsMapping ).when( dependencyC ).getBaseModuleIdsMapping();
    doReturn( dependencyC ).when( dependencyResolverFunction ).apply( eq( "@dep/C" ), anyString() );
    return dependencyResolverFunction;
  }
  // endregion
}
