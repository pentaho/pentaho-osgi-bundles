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
package org.pentaho.requirejs.impl.plugins;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ModulesInfoPluginConfigTest {
  private ModulesInfoPluginConfig plugin;

  @Before
  public void setup() {
    this.plugin = new ModulesInfoPluginConfig();
  }

  @Test
  public void applyModuleTypeConfig() {
    Map<String, Map<String, Map<String, Map<String, Object>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> config = new HashMap<>();
    Map<String, Map<String, Object>> module = new HashMap<>();

    Map<String, Object> moduleConfig = new HashMap<>();
    moduleConfig.put( "base", "baseA" );
    moduleConfig.put( "other", "A" );
    module.put( "moduleA", moduleConfig );

    moduleConfig = new HashMap<>();
    moduleConfig.put( "base", "baseB" );
    moduleConfig.put( "other", "B" );
    module.put( "moduleB", moduleConfig );

    config.put( "pentaho/modules", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> modulesInfo = requireConfig.get( "config" ).get( "pentaho/modules" );

    assertTrue( "Original moduleA removed", !modulesInfo.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", modulesInfo.containsKey( "moduleA_resolved" ) );

    assertTrue( "Original moduleB removed", !modulesInfo.containsKey( "moduleB" ) );
    assertTrue( "Added moduleB_resolved", modulesInfo.containsKey( "moduleB_resolved" ) );

    Map<String, Object> moduleA_resolved = modulesInfo.get( "moduleA_resolved" );
    assertEquals( "Base property is resolved", "baseA_resolved", moduleA_resolved.get( "base" ) );
    assertEquals( "Other properties are kept as is", "A", moduleA_resolved.get( "other" ) );

    Map<String, Object> moduleB_resolved = modulesInfo.get( "moduleB_resolved" );
    assertEquals( "Base property is resolved", "baseB_resolved", moduleB_resolved.get( "base" ) );
    assertEquals( "Other properties are kept as is", "B", moduleB_resolved.get( "other" ) );
  }

  @Test
  public void applyModuleNullTypeConfig() {
    Map<String, Map<String, Map<String, Map<String, Object>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> config = new HashMap<>();
    Map<String, Map<String, Object>> module = new HashMap<>();

    Map<String, Object> moduleConfig = new HashMap<>();
    moduleConfig.put( "base", null );
    module.put( "moduleA", moduleConfig );

    config.put( "pentaho/modules", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> modulesInfo = requireConfig.get( "config" ).get( "pentaho/modules" );

    Map<String, Object> moduleA_resolved = modulesInfo.get( "moduleA_resolved" );
    assertEquals( "Base property is resolved", null, moduleA_resolved.get( "base" ) );
  }

  @Test
  public void applyModuleInstanceConfig() {
    Map<String, Map<String, Map<String, Map<String, Object>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> config = new HashMap<>();
    Map<String, Map<String, Object>> module = new HashMap<>();

    Map<String, Object> moduleConfig = new HashMap<>();
    moduleConfig.put( "type", "baseA" );
    moduleConfig.put( "other", "A" );
    module.put( "moduleA", moduleConfig );

    moduleConfig = new HashMap<>();
    moduleConfig.put( "type", "baseB" );
    moduleConfig.put( "other", "B" );
    module.put( "moduleB", moduleConfig );

    config.put( "pentaho/modules", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> modulesInfo = requireConfig.get( "config" ).get( "pentaho/modules" );

    assertTrue( "Original moduleA removed", !modulesInfo.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", modulesInfo.containsKey( "moduleA_resolved" ) );

    assertTrue( "Original moduleB removed", !modulesInfo.containsKey( "moduleB" ) );
    assertTrue( "Added moduleB_resolved", modulesInfo.containsKey( "moduleB_resolved" ) );

    Map<String, Object> moduleA_resolved = modulesInfo.get( "moduleA_resolved" );
    assertEquals( "Type property is resolved", "baseA_resolved", moduleA_resolved.get( "type" ) );
    assertEquals( "Other properties are kept as is", "A", moduleA_resolved.get( "other" ) );

    Map<String, Object> moduleB_resolved = modulesInfo.get( "moduleB_resolved" );
    assertEquals( "Type property is resolved", "baseB_resolved", moduleB_resolved.get( "type" ) );
    assertEquals( "Other properties are kept as is", "B", moduleB_resolved.get( "other" ) );
  }

  @Test
  public void applyModuleAnnotationsConfig() {
    Map<String, Map<String, Map<String, Map<String, Object>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> config = new HashMap<>();
    Map<String, Map<String, Object>> module = new HashMap<>();

    Map<String, Object> moduleConfig = new HashMap<>();
    Map<String, Object> annotations = new HashMap<>();
    Object annotationA = new Object();
    annotations.put( "baseA", annotationA );

    Object annotationB = new Object();
    annotations.put( "baseB", annotationB );

    moduleConfig.put( "annotations", annotations );
    module.put( "moduleA", moduleConfig );

    config.put( "pentaho/modules", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> modulesInfo = requireConfig.get( "config" ).get( "pentaho/modules" );

    assertTrue( "Original moduleA removed", !modulesInfo.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", modulesInfo.containsKey( "moduleA_resolved" ) );

    Map<String, Object> moduleA_resolved = modulesInfo.get( "moduleA_resolved" );
    Map<String, Object> moduleA_annotations = ( Map<String, Object> ) moduleA_resolved.get( "annotations" );

    assertTrue( "Original baseA removed", !moduleA_annotations.containsKey( "baseA" ) );
    assertTrue( "Original baseB removed", !moduleA_annotations.containsKey( "baseB" ) );

    assertTrue( "Added baseA_resolved", moduleA_annotations.containsKey( "baseA_resolved" ) );
    assertTrue( "Added baseB_resolved", moduleA_annotations.containsKey( "baseB_resolved" ) );

    assertEquals( "Preserved baseA value", annotationA, moduleA_annotations.get( "baseA_resolved" ) );
    assertEquals( "Preserved baseB value", annotationB, moduleA_annotations.get( "baseB_resolved" ) );
  }

  @Test
  public void applyNoConfig() {
    Map<String, Object> requireConfig = new HashMap<>();

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyNoModulesConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    requireConfig.put( "config", new HashMap<>() );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyEmptyModulesConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "pentaho/modules", new HashMap<>() );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = NullPointerException.class)
  public void applyNullConfig() {
    this.plugin.apply( null, null, null, null );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    requireConfig.put( "config", 5 );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongModulesConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "pentaho/modules", 5 );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongModuleConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    HashMap<String, Object> module = new HashMap<>();
    module.put( "my/module", 5 );
    config.put( "pentaho/modules", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );
  }

  private Function<String, String> createResolveModuleIdFunction() {
    Function<String, String> resolveModuleId = mock( Function.class );
    doAnswer( invocation -> invocation.getArguments()[ 0 ] + "_resolved" ).when( resolveModuleId ).apply( anyString() );
    return resolveModuleId;
  }
}
