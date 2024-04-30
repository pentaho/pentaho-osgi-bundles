/*!
 * Copyright 2018-2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl.plugins;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.impl.types.MetaInfRequireJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class NomAmdPackageShimTest {
  private static final String EXPORTED_VAR = "A";

  private NomAmdPackageShim plugin;

  @Before
  public void setup() {
    this.plugin = new NomAmdPackageShim();
  }

  @Test
  public void applyNonAmdPackageWithNomAmdDependenciesAndWithExports() {
    Function<String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( Function.class );

    IRequireJsPackageConfiguration mockPackageConfiguration = createMockPackageConfiguration( createMockMetaInfRequireJson( false, EXPORTED_VAR ) );
    Map<String, String> baseMap = new HashMap<>();
    baseMap.put( "AA", "A_resolved_AA" );
    baseMap.put( "A/B", "A_resolved/B" );
    doReturn( baseMap ).when( mockPackageConfiguration ).getBaseModuleIdsMapping();

    doReturn( mockPackageConfiguration ).when( dependencyResolverFunction ).apply( eq( "depA" ) );

    doReturn( createMockRegularPackageConfiguration() ).when( dependencyResolverFunction ).apply( eq( "depB" ) );

    mockPackageConfiguration = createMockPackageConfiguration( createMockMetaInfRequireJson( false, EXPORTED_VAR ) );
    baseMap = new HashMap<>();
    baseMap.put( "BA", "B_resolved_BA" );
    baseMap.put( "B/B", "B_resolved/B" );
    doReturn( baseMap ).when( mockPackageConfiguration ).getBaseModuleIdsMapping();

    doReturn( mockPackageConfiguration ).when( dependencyResolverFunction ).apply( eq( "depC" ) );

    IRequireJsPackageConfiguration requirePackageConfig = createMockPackageConfiguration( createMockMetaInfRequireJson( false, EXPORTED_VAR ) );
    Map<String, String> dependencies = new HashMap<>();
    dependencies.put( "depA", "1.0.0" );
    dependencies.put( "depB", "1.5.0" );
    dependencies.put( "depC", "2.5.0" );
    doReturn( dependencies ).when( requirePackageConfig ).getDependencies();

    Map<String, Map<String, Map<String, ?>>> requireConfig = createEmptyRequireConfig();

    this.plugin.apply( requirePackageConfig, dependencyResolverFunction, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, ?>> shim = requireConfig.get( "shim" );

    assertEquals( 3, shim.size() );

    assertTrue( "Doesn't define original moduleA", !shim.containsKey( "moduleA" ) );
    assertTrue( "Defines moduleA_resolved", shim.containsKey( "moduleA_resolved" ) );

    assertTrue( "Doesn't define original moduleA/index", !shim.containsKey( "moduleA/index" ) );
    assertTrue( "Defines moduleA_resolved/index", shim.containsKey( "moduleA_resolved/index" ) );

    assertTrue( "Doesn't define original moduleB", !shim.containsKey( "moduleB" ) );
    assertTrue( "Defines moduleB_resolved", shim.containsKey( "moduleB_resolved" ) );

    Map<String, ?> moduleA_resolved = shim.get( "moduleA_resolved" );

    assertTrue( moduleA_resolved.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleA_resolved.get( "exports" ) );

    assertTrue( moduleA_resolved.containsKey( "deps" ) );

    List<String> deps = (List<String>) moduleA_resolved.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );

    Map<String, ?> moduleA_resolved_index = shim.get( "moduleA_resolved/index" );

    assertTrue( moduleA_resolved_index.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleA_resolved_index.get( "exports" ) );

    assertTrue( moduleA_resolved_index.containsKey( "deps" ) );

    deps = (List<String>) moduleA_resolved_index.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );

    Map<String, ?> moduleB_resolved = shim.get( "moduleB_resolved" );

    assertTrue( moduleB_resolved.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleB_resolved.get( "exports" ) );

    assertTrue( moduleB_resolved.containsKey( "deps" ) );

    deps = (List<String>) moduleB_resolved.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );
  }

  @Test
  public void applyNonAmdPackageWithNomAmdDependenciesAndNoExports() {
    Function<String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( Function.class );

    IRequireJsPackageConfiguration mockPackageConfiguration = createMockPackageConfiguration( createMockMetaInfRequireJson( false, "DONTCARE" ) );
    Map<String, String> baseMap = new HashMap<>();
    baseMap.put( "AA", "A_resolved_AA" );
    baseMap.put( "A/B", "A_resolved/B" );
    doReturn( baseMap ).when( mockPackageConfiguration ).getBaseModuleIdsMapping();

    doReturn( mockPackageConfiguration ).when( dependencyResolverFunction ).apply( eq( "depA" ) );

    doReturn( createMockRegularPackageConfiguration() ).when( dependencyResolverFunction ).apply( eq( "depB" ) );

    mockPackageConfiguration = createMockPackageConfiguration( createMockMetaInfRequireJson( false, "DONTCARE" ) );
    baseMap = new HashMap<>();
    baseMap.put( "BA", "B_resolved_BA" );
    baseMap.put( "B/B", "B_resolved/B" );
    doReturn( baseMap ).when( mockPackageConfiguration ).getBaseModuleIdsMapping();

    doReturn( mockPackageConfiguration ).when( dependencyResolverFunction ).apply( eq( "depC" ) );

    IRequireJsPackageConfiguration requirePackageConfig = createMockPackageConfiguration( createMockMetaInfRequireJson( false, null ) );
    Map<String, String> dependencies = new HashMap<>();
    dependencies.put( "depA", "1.0.0" );
    dependencies.put( "depB", "1.5.0" );
    dependencies.put( "depC", "2.5.0" );
    doReturn( dependencies ).when( requirePackageConfig ).getDependencies();

    Map<String, Map<String, Map<String, ?>>> requireConfig = createEmptyRequireConfig();

    this.plugin.apply( requirePackageConfig, dependencyResolverFunction, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, ?>> shim = requireConfig.get( "shim" );

    assertEquals( 3, shim.size() );

    assertTrue( "Doesn't define original moduleA", !shim.containsKey( "moduleA" ) );
    assertTrue( "Defines moduleA_resolved", shim.containsKey( "moduleA_resolved" ) );

    assertTrue( "Doesn't define original moduleA/index", !shim.containsKey( "moduleA/index" ) );
    assertTrue( "Defines moduleA_resolved/index", shim.containsKey( "moduleA_resolved/index" ) );

    assertTrue( "Doesn't define original moduleB", !shim.containsKey( "moduleB" ) );
    assertTrue( "Defines moduleB_resolved", shim.containsKey( "moduleB_resolved" ) );

    Map<String, ?> moduleA_resolved = shim.get( "moduleA_resolved" );

    assertTrue( !moduleA_resolved.containsKey( "exports" ) );

    assertTrue( moduleA_resolved.containsKey( "deps" ) );

    List<String> deps = (List<String>) moduleA_resolved.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );

    Map<String, ?> moduleA_resolved_index = shim.get( "moduleA_resolved/index" );

    assertTrue( !moduleA_resolved_index.containsKey( "exports" ) );

    assertTrue( moduleA_resolved_index.containsKey( "deps" ) );

    deps = (List<String>) moduleA_resolved_index.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );

    Map<String, ?> moduleB_resolved = shim.get( "moduleB_resolved" );

    assertTrue( !moduleB_resolved.containsKey( "exports" ) );

    assertTrue( moduleB_resolved.containsKey( "deps" ) );

    deps = (List<String>) moduleB_resolved.get( "deps" );
    assertEquals( 4, deps.size() );
    assertTrue( deps.contains( "A_resolved_AA" ) );
    assertTrue( deps.contains( "A_resolved/B" ) );
    assertTrue( deps.contains( "B_resolved_BA" ) );
    assertTrue( deps.contains( "B_resolved/B" ) );
  }

  @Test
  public void applyNonAmdPackageWithoutNonAmdDependenciesAndWithExports() {
    Function<String, IRequireJsPackageConfiguration> dependencyResolverFunction = mock( Function.class );
    doReturn( createMockPackageConfiguration( createMockMetaInfRequireJson( true, EXPORTED_VAR ) ) ).when( dependencyResolverFunction ).apply( eq( "depA" ) );
    doReturn( createMockRegularPackageConfiguration() ).when( dependencyResolverFunction ).apply( eq( "depB" ) );

    IRequireJsPackageConfiguration requirePackageConfig = createMockPackageConfiguration( createMockMetaInfRequireJson( false, EXPORTED_VAR ) );
    Map<String, String> dependencies = new HashMap<>();
    dependencies.put( "depA", "1.0.0" );
    dependencies.put( "depB", "1.5.0" );
    doReturn( dependencies ).when( requirePackageConfig ).getDependencies();

    Map<String, Map<String, Map<String, ?>>> requireConfig = createEmptyRequireConfig();

    this.plugin.apply( requirePackageConfig, dependencyResolverFunction, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, ?>> shim = requireConfig.get( "shim" );

    assertEquals( 3, shim.size() );

    assertTrue( "Doesn't define original moduleA", !shim.containsKey( "moduleA" ) );
    assertTrue( "Defines moduleA_resolved", shim.containsKey( "moduleA_resolved" ) );

    assertTrue( "Doesn't define original moduleA/index", !shim.containsKey( "moduleA/index" ) );
    assertTrue( "Defines moduleA_resolved/index", shim.containsKey( "moduleA_resolved/index" ) );

    assertTrue( "Doesn't define original moduleB", !shim.containsKey( "moduleB" ) );
    assertTrue( "Defines moduleB_resolved", shim.containsKey( "moduleB_resolved" ) );

    Map<String, ?> moduleA_resolved = shim.get( "moduleA_resolved" );
    assertTrue( moduleA_resolved.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleA_resolved.get( "exports" ) );
    assertTrue( !moduleA_resolved.containsKey( "deps" ) );

    Map<String, ?> moduleA_resolved_index = shim.get( "moduleA_resolved/index" );
    assertTrue( moduleA_resolved_index.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleA_resolved_index.get( "exports" ) );
    assertTrue( !moduleA_resolved_index.containsKey( "deps" ) );

    Map<String, ?> moduleB_resolved = shim.get( "moduleB_resolved" );
    assertTrue( moduleB_resolved.containsKey( "exports" ) );
    assertEquals( EXPORTED_VAR, moduleB_resolved.get( "exports" ) );
    assertTrue( !moduleB_resolved.containsKey( "deps" ) );
  }

  @Test
  public void applyNonAmdPackageWithoutDependenciesAndWithoutExports() {
    Map<String, Map<String, Map<String, ?>>> requireConfig = createEmptyRequireConfig();

    this.plugin.apply( createMockPackageConfiguration( createMockMetaInfRequireJson( false, null ) ), null, null, requireConfig );

    Map<String, Map<String, ?>> shim = requireConfig.get( "shim" );
    assertTrue( shim.isEmpty() );
  }

  @Test
  public void applyAmdPackage() {
    this.plugin.apply( createMockPackageConfiguration( createMockMetaInfRequireJson( true, null ) ), null, null, null );
  }

  @Test
  public void applyNotMetaInfRequireJsPackage() {
    this.plugin.apply( createMockRegularPackageConfiguration(), null, null, null );
  }

  @Test(expected = NullPointerException.class)
  public void applyNullPackage() {
    this.plugin.apply( null, null, null, null );
  }

  private IRequireJsPackageConfiguration createMockRegularPackageConfiguration() {
    return createMockPackageConfiguration( mock( IRequireJsPackage.class ) );
  }

  private IRequireJsPackageConfiguration createMockPackageConfiguration( IRequireJsPackage requireJsPackage ) {
    IRequireJsPackageConfiguration requirePackageConfig = mock( IRequireJsPackageConfiguration.class );
    doReturn( requireJsPackage ).when( requirePackageConfig ).getRequireJsPackage();

    return requirePackageConfig;
  }

  private MetaInfRequireJson createMockMetaInfRequireJson( Object isAmdPackage, String exportedVar ) {
    MetaInfRequireJson requireJsPackage = mock( MetaInfRequireJson.class );
    doReturn( isAmdPackage ).when( requireJsPackage ).isAmdPackage();

    doReturn( exportedVar ).when( requireJsPackage ).getExports();

    Map<String, String> modules = new HashMap<>();
    modules.put( "moduleA", "/some/module/A" );
    modules.put( "moduleB", "/other/module/B" );
    doReturn( modules ).when( requireJsPackage ).getModules();

    doReturn( "index" ).when( requireJsPackage ).getModuleMainFile( "moduleA" );

    return requireJsPackage;
  }

  private Function<String, String> createResolveModuleIdFunction() {
    Function<String, String> resolveModuleId = mock( Function.class );
    doAnswer( invocation -> invocation.getArguments()[ 0 ] + "_resolved" ).when( resolveModuleId ).apply( anyString() );
    return resolveModuleId;
  }

  private Map<String, Map<String, Map<String, ?>>> createEmptyRequireConfig() {
    Map<String, Map<String, Map<String, ?>>> requireConfig = new HashMap<>();
    requireConfig.put( "shim", new HashMap<>() );
    return requireConfig;
  }
}