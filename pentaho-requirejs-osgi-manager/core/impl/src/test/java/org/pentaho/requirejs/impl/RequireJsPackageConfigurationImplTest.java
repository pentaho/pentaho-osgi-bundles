package org.pentaho.requirejs.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.requirejs.RequireJsPackage;
import org.pentaho.requirejs.RequireJsPackageConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class RequireJsPackageConfigurationImplTest {

  private Map<String, String> modules;

  @Before
  public void setUp() {
    modules = new HashMap<>();
    modules.put( "some/module/A", "/some/path/to/module/A" );
    modules.put( "some/module/B", "/other/path/to/module/B" );
    modules.put( "at-root", "/the/root/one" );
  }

  @Test(expected = NullPointerException.class)
  public void requireJsPackageIsRequired() {
    new RequireJsPackageConfigurationImpl( null );
  }

  @Test
  public void getVersionedName() {
    String name = "@tests/basic";
    String version = "1.0.1";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version );

    assertEquals( "Should concatenate the name and the version using an underscore character", name + "_" + version, packageConfiguration.getVersionedName() );

    packageConfiguration = createRequireJsPackageConfiguration( null, version );
    assertEquals( "Should return null if name is null", null, packageConfiguration.getVersionedName() );

    packageConfiguration = createRequireJsPackageConfiguration( "", version );
    assertEquals( "Should return null if name is empty", null, packageConfiguration.getVersionedName() );

    packageConfiguration = createRequireJsPackageConfiguration( name, null );
    assertEquals( "Should return null if version is null", null, packageConfiguration.getVersionedName() );

    packageConfiguration = createRequireJsPackageConfiguration( name, "" );
    assertEquals( "Should return null if version is empty", null, packageConfiguration.getVersionedName() );
  }

  @Test
  public void getBaseModuleIdsMappingEmpty() {
    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration();

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertNotNull( "Should always return a base module IDs mapping", baseModuleIdsMapping );
    assertTrue( "Should return an empty base module IDs mapping", baseModuleIdsMapping.isEmpty() );
  }

  @Test
  public void getBaseModuleIdsMapping() {
    String name = "@tests/basic";
    String version = "1.0.1";

    // modules are processed at construction time, so the base modules IDs of all packages are available during dependency resolution
    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, null, this.modules );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    assertTrue( "Maps a versioned ID to each module", baseModuleIdsMapping.containsKey( "some/module/A" ) );
    assertTrue( "Maps a versioned ID to each module", baseModuleIdsMapping.containsKey( "some/module/B" ) );
    assertTrue( "Maps a versioned ID to each module", baseModuleIdsMapping.containsKey( "at-root" ) );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getBaseModuleIdsMappingReturnsUnmodifiableMap() {
    String name = "@tests/basic";
    String version = "1.0.1";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, null, this.modules );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();
    baseModuleIdsMapping.put( "moduleId", "mappedId" );
  }

  @Test
  public void getEmptyRequireConfig() {
    String name = "@tests/basic";
    String version = "1.0.1";
    String webRoot = "/web/path";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, webRoot );

    BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    assertTrue( "Should have empty paths map", ( (Map<String, ?>) requireConfig.get( "paths" ) ).isEmpty() );
    assertTrue( "Should have empty map map", ( (Map<String, ?>) requireConfig.get( "map" ) ).isEmpty() );
    assertTrue( "Should have empty packages list", ( (List<?>) requireConfig.get( "packages" ) ).isEmpty() );
    assertTrue( "Should have empty config map", ( (Map<String, ?>) requireConfig.get( "config" ) ).isEmpty() );
    assertTrue( "Should have empty shim map", ( (Map<String, ?>) requireConfig.get( "shim" ) ).isEmpty() );
  }

  @Test
  public void getRequireConfigPaths() {
    String name = "@tests/basic";
    String version = "1.0.1";
    String webRoot = "web/path";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, webRoot, modules );

    BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

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
  public void getRequireConfigMaps() {
    String name = "@tests/basic";
    String version = "1.0.1";
    String webRoot = "/web/path";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, webRoot, modules );

    BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

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
  public void getRequireConfigPackages() {
    String name = "@tests/basic";
    String version = "1.0.1";
    String webRoot = "/web/path";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, webRoot, modules );

    BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

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
  public void getRequireConfig() {
/* TO TEST:
2. dependency resolver is called for each dependency
3. Plugins are called, test the two functions and that the require config changes
4. Plugins are not allowed to change anything other than config and shim
7. The require config structure, with mappings for each module, including dependencies, etc.
8. getVersionedModuleId should be tested, the split by !, etc.
*/
    String name = "@tests/basic";
    String version = "1.0.1";
    String webRoot = "/web/path";

    RequireJsPackageConfigurationImpl packageConfiguration = createRequireJsPackageConfiguration( name, version, webRoot, modules );

    BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolverFunction = mock( BiFunction.class );
    packageConfiguration.processDependencies( dependencyResolverFunction );

    Map<String, String> baseModuleIdsMapping = packageConfiguration.getBaseModuleIdsMapping();

    Map<String, ?> requireConfig = packageConfiguration.getRequireConfig( null );

    Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) requireConfig.get( "map" );
    Map<String, String> paths = (Map<String, String>) requireConfig.get( "paths" );

    modules.forEach( ( moduleId, path ) -> {
      String versionedModuleID = baseModuleIdsMapping.get( moduleId );

      assertTrue( "Defines path for the versioned module ID", paths.containsKey( versionedModuleID ) );
      assertTrue( "Creates mappings for the versioned module ID", map.containsKey( versionedModuleID ) );

      Map<String, String> moduleMap = map.get( versionedModuleID );

      modules.keySet().forEach( ( internalModuleId ) -> assertTrue( "Maps the package's modules to each other", moduleMap.containsKey( internalModuleId ) ) );
    } );

    List<?> packages = (List<?>) requireConfig.get( "packages" );

    assertTrue( "Should have package in string format for some/module/B (main)", packages.contains( baseModuleIdsMapping.get( "some/module/B" ) ) );

    Map<String, String> packageDefinition = new HashMap<>( 2 );
    packageDefinition.put( "name", baseModuleIdsMapping.get( "at-root" ) );
    packageDefinition.put( "main", "special" );
    assertTrue( "Should have package in object format for at-root (special)", packages.contains( packageDefinition ) );

    assertTrue( "Should not have any other package (some/module/A is not a package)", packages.size() == 2 );

    assertTrue( "Should have empty config map", ( (Map<String, ?>) requireConfig.get( "config" ) ).isEmpty() );
    assertTrue( "Should have empty shim map", ( (Map<String, ?>) requireConfig.get( "shim" ) ).isEmpty() );
  }

  // region Access to the underlying RequireJsPackage
  @Test
  public void getRequireJsPackage() {
    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    assertSame( mockRequireJsPackage, packageConfiguration.getRequireJsPackage() );
  }

  @Test
  public void getName() {
    String name = "@tests/basic";

    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( name ).when( mockRequireJsPackage ).getName();
    assertEquals( "Should return RequireJsPackage name", name, packageConfiguration.getName() );

    doReturn( null ).when( mockRequireJsPackage ).getName();
    assertEquals( "Should return empty name if RequireJsPackage name is null", "", packageConfiguration.getName() );
  }

  @Test
  public void getVersion() {
    String version = "1.0.1";

    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( version ).when( mockRequireJsPackage ).getVersion();
    assertEquals( "Should return RequireJsPackage version", version, packageConfiguration.getVersion() );

    doReturn( null ).when( mockRequireJsPackage ).getVersion();
    assertEquals( "Should return empty version if RequireJsPackage version is null", "", packageConfiguration.getVersion() );
  }

  @Test
  public void getWebRootPath() {
    String webRoot = "some/path";

    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
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
    Map<String, String> dependencies = new HashMap<>();
    dependencies.put( "moduleA", "11.0.0" );
    dependencies.put( "moduleB", "2.5.1" );

    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
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
    Map<String, String> dependencies = new HashMap<>();
    dependencies.put( "moduleA", "11.0.0" );
    dependencies.put( "moduleB", "2.5.1" );

    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( dependencies ).when( mockRequireJsPackage ).getDependencies();

    Map<String, String> packageConfigurationDependencies = packageConfiguration.getDependencies();
    packageConfigurationDependencies.put( "moduleC", "7.8" );
  }

  @Test
  public void hasScript() {
    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( false ).when( mockRequireJsPackage ).hasScript( anyString() );
    doReturn( true ).when( mockRequireJsPackage ).hasScript( "preconfig" );

    assertEquals( "Should return RequireJsPackage hasScript result", true, packageConfiguration.hasScript( "preconfig" ) );
    assertEquals( "Should return RequireJsPackage hasScript result", false, packageConfiguration.hasScript( "postconfig" ) );
  }

  @Test
  public void getScriptResource() throws MalformedURLException {
    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );
    RequireJsPackageConfigurationImpl packageConfiguration = new RequireJsPackageConfigurationImpl( mockRequireJsPackage );

    doReturn( null ).when( mockRequireJsPackage ).getScriptResource( anyString() );
    URL toBeReturned = new URL( "file://some/path" );
    doReturn( toBeReturned ).when( mockRequireJsPackage ).getScriptResource( "preconfig" );

    assertSame( "Should return RequireJsPackage getScriptResource result", toBeReturned, packageConfiguration.getScriptResource( "preconfig" ) );
    assertEquals( "Should return RequireJsPackage getScriptResource result", null, packageConfiguration.getScriptResource( "postconfig" ) );
  }
  // endregion

  private RequireJsPackageConfigurationImpl createRequireJsPackageConfiguration() {
    return createRequireJsPackageConfiguration( null, null, null, null );
  }

  private RequireJsPackageConfigurationImpl createRequireJsPackageConfiguration( String name, String version ) {
    return createRequireJsPackageConfiguration( name, version, null, null );
  }

  private RequireJsPackageConfigurationImpl createRequireJsPackageConfiguration( String name, String version, String webRoot ) {
    return createRequireJsPackageConfiguration( name, version, webRoot, null );
  }

  private RequireJsPackageConfigurationImpl createRequireJsPackageConfiguration( String name, String version, String webRoot, Map<String, String> modules ) {
    RequireJsPackage mockRequireJsPackage = mock( RequireJsPackage.class );

    doReturn( name ).when( mockRequireJsPackage ).getName();
    doReturn( version ).when( mockRequireJsPackage ).getVersion();
    doReturn( webRoot ).when( mockRequireJsPackage ).getWebRootPath();

    if ( modules != null ) {
      doReturn( modules ).when( mockRequireJsPackage ).getModules();

      doReturn( "main" ).when( mockRequireJsPackage ).getModuleMainFile( "some/module/B" );
      doReturn( "special" ).when( mockRequireJsPackage ).getModuleMainFile( "at-root" );
    }

    return new RequireJsPackageConfigurationImpl( mockRequireJsPackage );
  }
}