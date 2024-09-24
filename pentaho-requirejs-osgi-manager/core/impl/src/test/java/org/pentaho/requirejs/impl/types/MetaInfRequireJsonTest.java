/*!
 * Copyright 2018 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl.types;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MetaInfRequireJsonTest {
  private static final int TEST_FILE_BASE_NUMBER_OF_MODULES = 6;
  private static final int TEST_FILE_BASE_NUMBER_OF_CONFIGS = 2;
  private static final int TEST_FILE_BASE_NUMBER_OF_MAPS = 2;
  private static final int TEST_FILE_BASE_NUMBER_OF_SHIMS = 5;
  private static final int TEST_FILE_BASE_NUMBER_OF_DEPS = 3;

  private HashMap<String, Object> requireMeta;

  @Before
  public void setUp() throws Exception {
    requireMeta = (HashMap<String, Object>) (new JSONParser()).parse( new InputStreamReader( this.getClass().getResourceAsStream( "/require.meta.json" ) ) );
  }

  @Test
  public void getName() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "Get name from only module of first version of first artifact info", "angular", requireJson.getName() );
  }

  @Test
  public void getNameMultipleModules() {
    ( (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" ) ).get( "artifacts" ).get( "org.webjars/angularjs" ).get( "1.3.0-SNAPSHOT" ).put( "5.6", new HashMap<>() );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "Get name from first artifact info", "angularjs", requireJson.getName() );
  }

  @Test
  public void getNameNoArtifactInfo() {
    ( (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" ) ).remove( "artifacts" );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "Get name from first module info", "angular", requireJson.getName() );
  }

  @Test
  public void getNameNoArtifactAndModuleInfo() {
    final Map<String, Map<String, Map<String, Map<String, Object>>>> meta = (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" );
    meta.remove( "artifacts" );
    meta.remove( "modules" );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "", requireJson.getName() );
  }

  @Test
  public void getVersion() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "1.3.0-SNAPSHOT", requireJson.getVersion() );
  }

  @Test
  public void getWebRootPath() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( "angular@1.3.0-SNAPSHOT", requireJson.getWebRootPath() );
  }

  @Test
  public void preferGlobal() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( false, requireJson.preferGlobal() );
  }

  @Test
  public void preferGlobalNoNAme() {
    final Map<String, Map<String, Map<String, Map<String, Object>>>> meta = (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" );
    meta.remove( "artifacts" );
    meta.remove( "modules" );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertEquals( true, requireJson.preferGlobal() );
  }

  @Test
  public void getModules() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES, modules.size() );
    assertTrue( "Remove versioning from known modules", modules.containsKey( "angular" ) );
    assertTrue( "Remove versioning from known modules", modules.containsKey( "angular-ui-router.stateHelper" ) );
    assertTrue( "Remove versioning from known modules", modules.containsKey( "smart-table-min" ) );
    assertTrue( "Remove versioning from known modules", modules.containsKey( "smart-table" ) );

    assertTrue( "Leave as-is unknown modules", modules.containsKey( "other" ) );
    assertTrue( "Leave as-is unknown modules", modules.containsKey( "simple" ) );

    assertEquals( "Resolve known locations", "/some-folder", modules.get( "other" ) );
  }

  @Test
  public void getModuleMainFile() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    assertNull( requireJson.getModuleMainFile( "angular" ) );
    assertNull( requireJson.getModuleMainFile( "smart-table-min" ) );
    assertNull( requireJson.getModuleMainFile( "smart-table" ) );

    assertEquals( "statehelper", requireJson.getModuleMainFile( "angular-ui-router.stateHelper" ) );
    assertEquals( "main", requireJson.getModuleMainFile( "other" ) );
    assertEquals( "main", requireJson.getModuleMainFile( "simple" ) );
  }

  @Test
  public void getDependencies() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> dependencies = requireJson.getDependencies();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_DEPS, dependencies.size() );
  }

  @Test
  public void hasScript() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertFalse( requireJson.hasScript( "preconfig" ) );
  }

  @Test
  public void getScriptResource() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertNull( requireJson.getScriptResource( "preconfig" ) );
  }

  @Test
  public void getConfig() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, ?> config = requireJson.getConfig();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_CONFIGS, config.size() );
    assertTrue( "Remove versioning from known modules", config.containsKey( "smart-table" ) );
    assertTrue( "Leave as-is unknown modules", config.containsKey( "something-unknown@2.0.1" ) );
  }

  @Test
  public void getMap() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS, map.size() );

    assertTrue( "Remove versioning from known modules", map.containsKey( "smart-table" ) );
    assertTrue( "Leave as-is unknown modules", map.get( "smart-table" ).containsKey( "other-unknown@3.0.7" ) );
    assertEquals( "Leave as-is unknown modules", "better-unknown@3.0.0", map.get( "smart-table" ).get( "other-unknown@3.0.7" ) );

    assertTrue( "Leave as-is unknown modules", map.containsKey( "something-unknown@2.0.1" ) );
    assertTrue( "Remove versioning from known modules", map.get( "something-unknown@2.0.1" ).containsKey( "angular" ) );
    assertEquals( "Remove versioning from known modules", "angular-ui-router.stateHelper", map.get( "something-unknown@2.0.1" ).get( "angular" ) );
  }

  @Test
  public void getShim() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    Map<String, Map<String, ?>> shim = requireJson.getShim();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_SHIMS, shim.size() );

    assertTrue( "Leave as-is unknown modules", shim.containsKey( "something-unknown@2.0.1" ) );
    assertTrue( "Remove versioning from known modules", shim.containsKey( "angular" ) );
    assertTrue( "Remove versioning from known modules", shim.containsKey( "angular-ui-router.stateHelper" ) );
    assertTrue( "Remove versioning from known modules", shim.containsKey( "angular-ui-router.stateHelper/statehelper" ) );
    assertTrue( "Remove versioning from known modules", shim.containsKey( "smart-table" ) );
  }

  @Test
  public void isAmdPackage() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertTrue( requireJson.isAmdPackage() );
  }

  @Test
  public void isAmdPackageOneNot() {
    final Map<String, Map<String, Map<String, Map<String, Object>>>> meta = (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" );
    meta.get( "modules" ).get( "smart-table" ).get( "2.0.3-1" ).put( "isAmdPackage", false );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertFalse( requireJson.isAmdPackage() );
  }

  @Test
  public void getExports() {
    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertNull( requireJson.getExports() );
  }

  @Test
  public void getExportsLastNonAmd() {
    final Map<String, Map<String, Map<String, Map<String, Object>>>> meta = (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" );
    meta.get( "modules" ).get( "smart-table" ).get( "2.0.3-1" ).put( "isAmdPackage", false );
    meta.get( "modules" ).get( "smart-table" ).get( "2.0.3-1" ).put( "exports", "var1" );

    meta.get( "modules" ).get( "smart-table-min" ).get( "2.0.3-1" ).put( "isAmdPackage", false );
    meta.get( "modules" ).get( "smart-table-min" ).get( "2.0.3-1" ).put( "exports", "var2" );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertEquals( "var2", requireJson.getExports() );
  }

  @Test
  public void getExportsOnlyIfNonAmd() {
    final Map<String, Map<String, Map<String, Map<String, Object>>>> meta = (Map<String, Map<String, Map<String, Map<String, Object>>>>) requireMeta.get( "requirejs-osgi-meta" );
    meta.get( "modules" ).get( "smart-table" ).get( "2.0.3-1" ).put( "isAmdPackage", true );
    meta.get( "modules" ).get( "smart-table" ).get( "2.0.3-1" ).put( "exports", "var1" );

    meta.get( "modules" ).get( "smart-table-min" ).get( "2.0.3-1" ).put( "isAmdPackage", true );
    meta.get( "modules" ).get( "smart-table-min" ).get( "2.0.3-1" ).put( "exports", "var2" );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );
    assertNull( requireJson.getExports() );
  }

  @Test
  public void testOverridesAddPath() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> paths = new HashMap<>();
    overrides.put( "paths", paths );

    paths.put( "newPath", "asdsa" );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES + 1, modules.size() );

    assertTrue( modules.containsKey( "newPath" ) );
  }

  @Test
  public void testOverridesRemovePath() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> paths = new HashMap<>();
    overrides.put( "paths", paths );

    paths.put( "smart-table", null );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES - 1, modules.size() );

    assertFalse( modules.containsKey( "smart-table" ) );
  }

  @Test
  public void testOverridesReplacesPath() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> paths = new HashMap<>();
    overrides.put( "paths", paths );

    paths.put( "angular", "/some/new/path" );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES, modules.size() );

    assertEquals( "/some/new/path", modules.get( "angular" ) );
  }

  @Test
  public void testOverridesAddPackages() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final List<Object> packs = new ArrayList<>();
    overrides.put( "packages", packs );

    packs.add( "newPack" );
    HashMap<String, String> otherPack = new HashMap<>();
    otherPack.put( "name", "yetAnotherPack" );
    otherPack.put( "location", "/extra-packs" );
    otherPack.put( "main", "top" );

    packs.add( otherPack );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES + 2, modules.size() );

    assertTrue( modules.containsKey( "newPack" ) );
    assertEquals( "/newPack", modules.get( "newPack" ) );
    assertEquals( "main", requireJson.getModuleMainFile( "newPack" ) );

    assertTrue( modules.containsKey( "yetAnotherPack" ) );
    assertEquals( "/extra-packs", modules.get( "yetAnotherPack" ) );
    assertEquals( "top", requireJson.getModuleMainFile( "yetAnotherPack" ) );
  }

  @Test
  public void testOverridesReplacePackage() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final List<Object> packs = new ArrayList<>();
    overrides.put( "packages", packs );

    HashMap<String, String> otherPack = new HashMap<>();
    otherPack.put( "name", "other" );
    otherPack.put( "location", "/different/path" );
    otherPack.put( "main", "different-main" );

    packs.add( otherPack );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> modules = requireJson.getModules();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MODULES, modules.size() );

    assertTrue( modules.containsKey( "other" ) );
    assertEquals( "/different/path", modules.get( "other" ) );
    assertEquals( "different-main", requireJson.getModuleMainFile( "other" ) );
  }

  @Test
  public void testOverridesAddConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> configOverrides = new HashMap<>();
    overrides.put( "config", configOverrides );

    configOverrides.put( "newConfig", new HashMap<>() );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> config = requireJson.getConfig();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_CONFIGS + 1, config.size() );

    assertTrue( config.containsKey( "newConfig" ) );
  }

  @Test
  public void testOverridesRemoveConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> configOverrides = new HashMap<>();
    overrides.put( "config", configOverrides );

    configOverrides.put( "smart-table", null );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> config = requireJson.getConfig();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_CONFIGS - 1, config.size() );

    assertFalse( config.containsKey( "smart-table" ) );
  }

  @Test
  public void testOverridesReplacesConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> configOverrides = new HashMap<>();
    overrides.put( "config", configOverrides );

    final HashMap<Object, Object> newConfig = new HashMap<>();
    newConfig.put( "prop", "value" );
    configOverrides.put( "smart-table", newConfig );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> config = requireJson.getConfig();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_CONFIGS, config.size() );

    assertTrue( config.containsKey( "smart-table" ) );
    assertTrue( config.get( "smart-table" ).containsKey( "prop" ) );
    assertEquals( "value", config.get( "smart-table" ).get( "prop" ) );
  }

  @Test
  public void testOverridesAddMapConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> mapOverrides = new HashMap<>();
    overrides.put( "map", mapOverrides );

    final HashMap<Object, Object> newMap = new HashMap<>();
    newMap.put( "original", "mapped" );
    mapOverrides.put( "newMap", newMap );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS + 1, map.size() );

    assertTrue( map.containsKey( "newMap" ) );
    assertEquals( 1, map.get( "newMap" ).size() );
    assertTrue( map.get( "newMap" ).containsKey( "original" ) );
    assertEquals( "mapped", map.get( "newMap" ).get( "original" ) );
  }

  @Test
  public void testOverridesAddMap() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> mapOverrides = new HashMap<>();
    overrides.put( "map", mapOverrides );

    final HashMap<Object, Object> newMap = new HashMap<>();
    newMap.put( "original", "mapped" );
    mapOverrides.put( "smart-table", newMap );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS, map.size() );

    assertTrue( map.containsKey( "smart-table" ) );
    assertEquals( 3, map.get( "smart-table" ).size() );
    assertTrue( map.get( "smart-table" ).containsKey( "original" ) );
    assertEquals( "mapped", map.get( "smart-table" ).get( "original" ) );
  }

  @Test
  public void testOverridesRemoveMapConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> mapOverrides = new HashMap<>();
    overrides.put( "map", mapOverrides );

    mapOverrides.put( "smart-table", null );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS - 1, map.size() );

    assertFalse( map.containsKey( "smart-table" ) );
  }

  @Test
  public void testOverridesRemoveMap() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> mapOverrides = new HashMap<>();
    overrides.put( "map", mapOverrides );

    final HashMap<Object, Object> newMap = new HashMap<>();
    newMap.put( "angular", null );
    mapOverrides.put( "smart-table", newMap );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS, map.size() );

    assertTrue( map.containsKey( "smart-table" ) );
    assertEquals( 1, map.get( "smart-table" ).size() );
    assertFalse( map.get( "smart-table" ).containsKey( "angular" ) );
  }

  @Test
  public void testOverridesReplacesMap() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> mapOverrides = new HashMap<>();
    overrides.put( "map", mapOverrides );

    final HashMap<Object, Object> newMap = new HashMap<>();
    newMap.put( "angular", "the-new-module" );
    mapOverrides.put( "smart-table", newMap );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, String>> map = requireJson.getMap();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_MAPS, map.size() );

    assertTrue( map.containsKey( "smart-table" ) );
    assertTrue( map.get( "smart-table" ).containsKey( "angular" ) );
    assertEquals( "the-new-module", map.get( "smart-table" ).get( "angular" ) );
  }

  @Test
  public void testOverridesAddShimConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> shimOverrides = new HashMap<>();
    overrides.put( "shim", shimOverrides );

    final HashMap<Object, Object> newShim = new HashMap<>();
    newShim.put( "original", "shimped" );
    shimOverrides.put( "newShim", newShim );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> shim = requireJson.getShim();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_SHIMS + 1, shim.size() );

    assertTrue( shim.containsKey( "newShim" ) );
    assertEquals( 1, shim.get( "newShim" ).size() );
    assertTrue( shim.get( "newShim" ).containsKey( "original" ) );
    assertEquals( "shimped", shim.get( "newShim" ).get( "original" ) );
  }

  @Test
  public void testOverridesRemoveShimConfig() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> shimOverrides = new HashMap<>();
    overrides.put( "shim", shimOverrides );

    shimOverrides.put( "smart-table", null );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> shim = requireJson.getShim();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_SHIMS - 1, shim.size() );

    assertFalse( shim.containsKey( "smart-table" ) );
  }

  @Test
  public void testOverridesReplacesShim() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> shimOverrides = new HashMap<>();
    overrides.put( "shim", shimOverrides );

    final HashMap<Object, Object> newShim = new HashMap<>();
    newShim.put( "angular", "the-new-module" );
    shimOverrides.put( "smart-table", newShim );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, Map<String, ?>> shim = requireJson.getShim();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_SHIMS, shim.size() );

    assertTrue( shim.containsKey( "smart-table" ) );
    assertTrue( shim.get( "smart-table" ).containsKey( "angular" ) );
    assertEquals( "the-new-module", shim.get( "smart-table" ).get( "angular" ) );
  }

  @Test
  public void testOverridesAddDependency() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> dependencies = new HashMap<>();
    overrides.put( "dependencies", dependencies );

    dependencies.put( "newDependency", "1.2" );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> deps = requireJson.getDependencies();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_DEPS + 1, deps.size() );

    assertTrue( deps.containsKey( "newDependency" ) );
    assertEquals( "1.2", deps.get( "newDependency" ) );
  }

  @Test
  public void testOverridesRemoveDependency() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> dependencies = new HashMap<>();
    overrides.put( "dependencies", dependencies );

    dependencies.put( "angular", null );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> deps = requireJson.getDependencies();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_DEPS - 1, deps.size() );

    assertFalse( deps.containsKey( "angular" ) );
  }

  @Test
  public void testOverridesReplacesDependency() {
    final Map<String, Object> meta = (Map<String, Object>) requireMeta.get( "requirejs-osgi-meta" );

    Map<String, Object> overrides = new HashMap<>();
    final HashMap<String, Object> dependencies = new HashMap<>();
    overrides.put( "dependencies", dependencies );

    dependencies.put( "angular", "9.8" );

    meta.put( "overrides", overrides );

    MetaInfRequireJson requireJson = new MetaInfRequireJson( requireMeta );

    final Map<String, String> deps = requireJson.getDependencies();
    assertEquals( TEST_FILE_BASE_NUMBER_OF_DEPS, deps.size() );

    assertEquals( "9.8", deps.get( "angular" ) );
  }
}
