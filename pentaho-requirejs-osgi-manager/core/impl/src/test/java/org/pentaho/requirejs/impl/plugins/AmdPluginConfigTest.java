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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class AmdPluginConfigTest {
  private AmdPluginConfig plugin;

  @Before
  public void setup() {
    this.plugin = new AmdPluginConfig();
  }

  @Test
  public void applyShimAmdConfig() {
    Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Map<String, Object>>>> config = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> amd = new HashMap<>();

    Map<String, Map<String, Object>> shim = new HashMap<>();

    Map<String, Object> moduleA = new HashMap<>();
    moduleA.put( "exports", "A" );
    Map<String, String> depsA = new HashMap<>();
    depsA.put( "depA", "dA" );
    depsA.put( "depB", "dB" );
    moduleA.put( "deps", depsA );
    shim.put( "moduleA", moduleA );

    shim.put( "moduleB", new HashMap<>() );

    amd.put( "shim", shim );
    config.put( "amd", amd );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> shimAmdConfig = requireConfig.get( "config" ).get( "amd" ).get( "shim" );

    assertTrue( "Original moduleA removed", !shimAmdConfig.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", shimAmdConfig.containsKey( "moduleA_resolved" ) );

    assertTrue( "Original moduleB removed", !shimAmdConfig.containsKey( "moduleB" ) );
    assertTrue( "Added moduleB_resolved", shimAmdConfig.containsKey( "moduleB_resolved" ) );

    Map<String, Object> moduleA_resolved = shimAmdConfig.get( "moduleA_resolved" );
    assertEquals( "Other fields kept as is", "A", moduleA_resolved.get( "exports" ) );

    Map<String, Object> shimAmdConfigModuleDeps = (Map<String, Object>) moduleA_resolved.get( "deps" );
    assertTrue( "Original depA removed", !shimAmdConfigModuleDeps.containsKey( "depA" ) );
    assertTrue( "Added depA_resolved", shimAmdConfigModuleDeps.containsKey( "depA_resolved" ) );
    assertEquals( "Dep exports kept as is", "dA", shimAmdConfigModuleDeps.get( "depA_resolved" ) );

    assertTrue( "Original depB removed", !shimAmdConfigModuleDeps.containsKey( "depB" ) );
    assertTrue( "Added depB_resolved", shimAmdConfigModuleDeps.containsKey( "depB_resolved" ) );
    assertEquals( "Dep exports kept as is", "dB", shimAmdConfigModuleDeps.get( "depB_resolved" ) );
  }

  @Test
  public void applyNoConfig() {
    Map<String, Object> requireConfig = new HashMap<>();

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyNoAmdConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    requireConfig.put( "config", new HashMap<>() );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyNoShimAmdConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "amd", new HashMap<>() );
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
  public void applyWrongAmdConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "amd", 5 );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongShimAmdConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    HashMap<String, Object> amd = new HashMap<>();
    amd.put( "shim", 5 );
    config.put( "amd", amd );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongDepsShimAmdConfig() {
    Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>> requireConfig = new HashMap<>();
    Map<String, Map<String, Map<String, Map<String, Object>>>> config = new HashMap<>();
    Map<String, Map<String, Map<String, Object>>> amd = new HashMap<>();

    Map<String, Map<String, Object>> shim = new HashMap<>();

    Map<String, Object> moduleA = new HashMap<>();
    moduleA.put( "deps", 5 );
    shim.put( "moduleA", moduleA );

    amd.put( "shim", shim );
    config.put( "amd", amd );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );
  }

  private Function<String, String> createResolveModuleIdFunction() {
    Function<String, String> resolveModuleId = mock( Function.class );
    doAnswer( invocation -> invocation.getArguments()[ 0 ] + "_resolved" ).when( resolveModuleId ).apply( anyString() );
    return resolveModuleId;
  }
}