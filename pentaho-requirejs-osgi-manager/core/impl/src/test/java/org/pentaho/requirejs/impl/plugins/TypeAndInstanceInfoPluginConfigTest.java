package org.pentaho.requirejs.impl.plugins;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TypeAndInstanceInfoPluginConfigTest {
  private TypeAndInstanceInfoPluginConfig plugin;

  @Before
  public void setup() {
    this.plugin = new TypeAndInstanceInfoPluginConfig();
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

    config.put( "pentaho/typeInfo", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> typeInfo = requireConfig.get( "config" ).get( "pentaho/typeInfo" );

    assertTrue( "Original moduleA removed", !typeInfo.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", typeInfo.containsKey( "moduleA_resolved" ) );

    assertTrue( "Original moduleB removed", !typeInfo.containsKey( "moduleB" ) );
    assertTrue( "Added moduleB_resolved", typeInfo.containsKey( "moduleB_resolved" ) );

    Map<String, Object> moduleA_resolved = typeInfo.get( "moduleA_resolved" );
    assertEquals( "Base property is resolved", "baseA_resolved", moduleA_resolved.get( "base" ) );
    assertEquals( "Other properties are kept as is", "A", moduleA_resolved.get( "other" ) );

    Map<String, Object> moduleB_resolved = typeInfo.get( "moduleB_resolved" );
    assertEquals( "Base property is resolved", "baseB_resolved", moduleB_resolved.get( "base" ) );
    assertEquals( "Other properties are kept as is", "B", moduleB_resolved.get( "other" ) );
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

    config.put( "pentaho/instanceInfo", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );

    Map<String, Map<String, Object>> instanceInfo = requireConfig.get( "config" ).get( "pentaho/instanceInfo" );

    assertTrue( "Original moduleA removed", !instanceInfo.containsKey( "moduleA" ) );
    assertTrue( "Added moduleA_resolved", instanceInfo.containsKey( "moduleA_resolved" ) );

    assertTrue( "Original moduleB removed", !instanceInfo.containsKey( "moduleB" ) );
    assertTrue( "Added moduleB_resolved", instanceInfo.containsKey( "moduleB_resolved" ) );

    Map<String, Object> moduleA_resolved = instanceInfo.get( "moduleA_resolved" );
    assertEquals( "Type property is resolved", "baseA_resolved", moduleA_resolved.get( "type" ) );
    assertEquals( "Other properties are kept as is", "A", moduleA_resolved.get( "other" ) );

    Map<String, Object> moduleB_resolved = instanceInfo.get( "moduleB_resolved" );
    assertEquals( "Type property is resolved", "baseB_resolved", moduleB_resolved.get( "type" ) );
    assertEquals( "Other properties are kept as is", "B", moduleB_resolved.get( "other" ) );
  }

  @Test
  public void applyNoConfig() {
    Map<String, Object> requireConfig = new HashMap<>();

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyNoTypeOrInstanceConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    requireConfig.put( "config", new HashMap<>() );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test
  public void applyEmptyTypeOrInstanceConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "pentaho/typeInfo", new HashMap<>() );
    config.put( "pentaho/instanceInfo", new HashMap<>() );
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
  public void applyWrongTypeConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "pentaho/typeInfo", 5 );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongInstanceConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    config.put( "pentaho/instanceInfo", 5 );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, null, requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongModuleTypeConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    HashMap<String, Object> module = new HashMap<>();
    module.put( "my/module", 5 );
    config.put( "pentaho/typeInfo", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );
  }

  @Test(expected = ClassCastException.class)
  public void applyWrongModuleInstanceConfig() {
    Map<String, Object> requireConfig = new HashMap<>();
    HashMap<String, Object> config = new HashMap<>();
    HashMap<String, Object> module = new HashMap<>();
    module.put( "my/module", 5 );
    config.put( "pentaho/instanceInfo", module );
    requireConfig.put( "config", config );

    this.plugin.apply( null, null, createResolveModuleIdFunction(), requireConfig );
  }

  private Function<String, String> createResolveModuleIdFunction() {
    Function<String, String> resolveModuleId = mock( Function.class );
    doAnswer( invocation -> invocation.getArguments()[ 0 ] + "_resolved" ).when( resolveModuleId ).apply( anyString() );
    return resolveModuleId;
  }
}