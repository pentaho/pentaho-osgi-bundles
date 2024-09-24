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

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.impl.listeners.RequireJsBundleListener;
import org.pentaho.requirejs.impl.listeners.RequireJsPackageServiceTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RequireJsConfigManagerTest {
  private String baseUrl;

  private RequireJsConfigManager requireJsConfigManager;
  private RequireJsBundleListener mockExternalResourcesScriptsTracker;
  private RequireJsPackageServiceTracker mockPackageConfigurationsTracker;

  private HashMap<String, String> packageConfigurationMapping;

  @Before
  public void setup() throws Exception {
    this.baseUrl = "/default/base/url/";

    this.mockPackageConfigurationsTracker = mock( RequireJsPackageServiceTracker.class );
    this.mockExternalResourcesScriptsTracker = mock( RequireJsBundleListener.class );

    this.requireJsConfigManager = new RequireJsConfigManager();

    this.requireJsConfigManager.setPackageConfigurationsTracker( this.mockPackageConfigurationsTracker );
    this.requireJsConfigManager.setExternalResourcesScriptsTracker( this.mockExternalResourcesScriptsTracker );
    this.requireJsConfigManager.setPlugins( new ArrayList<>() );
  }

  @Test
  public void testDestroy() {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    spyed.destroy();

    // check that invalidateCachedConfigurations is called
    verify( spyed, times( 1 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfigTimeout() throws ExecutionException, InterruptedException {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    Future mockFuture = mock( Future.class );
    doThrow( InterruptedException.class ).when( mockFuture ).get();
    doReturn( mockFuture ).when( spyed ).getCachedConfiguration( this.baseUrl );

    String config = spyed.getRequireJsConfig( this.baseUrl );

    assertTrue( config.contains( "Error computing RequireJS Config" ) );

    // check that invalidateCachedConfigurations is not called
    verify( spyed, times( 0 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfigException() throws ExecutionException, InterruptedException {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    Future mockFuture = mock( Future.class );
    doThrow( ExecutionException.class ).when( mockFuture ).get();
    doReturn( mockFuture ).when( spyed ).getCachedConfiguration( this.baseUrl );

    String config = spyed.getRequireJsConfig( this.baseUrl );

    assertTrue( config.contains( "Error computing RequireJS Config" ) );

    // check that invalidateCachedConfigurations is called
    verify( spyed, atLeastOnce() ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfigExceptionWithCause() throws ExecutionException, InterruptedException {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    Future mockFuture = mock( Future.class );
    ExecutionException executionException = new ExecutionException( new RuntimeException( "The cause" ) );
    doThrow( executionException ).when( mockFuture ).get();
    doReturn( mockFuture ).when( spyed ).getCachedConfiguration( this.baseUrl );

    String config = spyed.getRequireJsConfig( this.baseUrl );

    assertTrue( config.contains( "Error computing RequireJS Config: The cause" ) );

    // check that invalidateCachedConfigurations is called
    verify( spyed, atLeastOnce() ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfig() throws Exception {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    Callable mockCallable = mock( Callable.class );
    doReturn( "The content of the requirejs configuration script" ).when( mockCallable ).call();

    doReturn( mockCallable ).when( spyed ).createRebuildCacheCallable( this.baseUrl );

    String config = spyed.getRequireJsConfig( this.baseUrl );

    assertEquals( "The content of the requirejs configuration script", config );

    verify( spyed, times( 1 ) ).createRebuildCacheCallable( this.baseUrl );
  }

  @Test
  public void testGetRequireJsConfigBaseUrlNormalization() throws Exception {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    spyed.getRequireJsConfig( "/base1/" );
    verify( spyed, times( 1 ) ).getCachedConfiguration( "/base1/" );

    spyed.getRequireJsConfig( "/base1" );
    verify( spyed, times( 2 ) ).getCachedConfiguration( "/base1/" );
  }

  @Test
  public void testGetRequireJsConfigCache() throws Exception {
    RequireJsConfigManager spyed = spy( this.requireJsConfigManager );

    Callable mockCallable = mock( Callable.class );
    doReturn( "The content of the requirejs configuration script" ).when( mockCallable ).call();

    doReturn( mockCallable ).when( spyed ).createRebuildCacheCallable( anyString() );

    spyed.getRequireJsConfig( "/base1/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base1/" );
    verify( spyed, times( 0 ) ).createRebuildCacheCallable( "/base2/" );

    spyed.getRequireJsConfig( "/base2/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base1/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base2/" );

    spyed.getRequireJsConfig( "/base1/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base1/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base2/" );

    spyed.getRequireJsConfig( "/base2/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base1/" );
    verify( spyed, times( 1 ) ).createRebuildCacheCallable( "/base2/" );
  }

  @Test
  public void testGetContextMappingKnownReferer() {
    Collection<IRequireJsPackageConfiguration> requireJsPackages = new ArrayList<>();
    requireJsPackages.add( createRequireJsPackageConfigurationMock( "package/1.0" ) );
    doReturn( requireJsPackages ).when( this.mockPackageConfigurationsTracker ).getPackages();

    String config = this.requireJsConfigManager.getContextMapping( this.baseUrl, "/default/base/url/package/1.0/index.html" );

    HashMap<String, Object> topMap = new HashMap<>();
    topMap.put( "*", packageConfigurationMapping );

    HashMap<String, Object> requireConfig = new HashMap<>();
    requireConfig.put( "map", topMap );

    assertEquals( "Configuration for context mapping should be the same of the package", JSONObject.toJSONString( requireConfig ), config );
  }

  @Test
  public void testGetContextMappingNullReferer() {
    String config = this.requireJsConfigManager.getContextMapping( this.baseUrl, null );

    assertNull( "No configuration for context mapping should be returned", config );
  }

  @Test
  public void testGetContextMappingEmptyReferer() {
    String config = this.requireJsConfigManager.getContextMapping( this.baseUrl, "" );

    assertNull( "No configuration for context mapping should be returned", config );
  }

  @Test
  public void testGetContextMappingUnknownReferer() {
    Collection<IRequireJsPackageConfiguration> requireJsPackages = new ArrayList<>();
    requireJsPackages.add( createRequireJsPackageConfigurationMock( "package/1.0" ) );
    doReturn( requireJsPackages ).when( this.mockPackageConfigurationsTracker ).getPackages();

    String config = this.requireJsConfigManager.getContextMapping( this.baseUrl, "/something/index.html" );

    assertNull( "No configuration for context mapping should be returned", config );
  }

  @Test
  public void testGetContextMappingCache() {
    Collection<IRequireJsPackageConfiguration> requireJsPackages = new ArrayList<>();
    IRequireJsPackageConfiguration packageA = createRequireJsPackageConfigurationMock( "packageA/1.0" );
    IRequireJsPackageConfiguration packageB = createRequireJsPackageConfigurationMock( "packageB/1.5" );
    requireJsPackages.add( packageA );
    requireJsPackages.add( packageB );
    doReturn( requireJsPackages ).when( this.mockPackageConfigurationsTracker ).getPackages();

    this.requireJsConfigManager.getContextMapping( this.baseUrl, "/default/base/url/packageA/1.0/index.html" );

    verify( packageA, times( 1 ) ).getModuleIdsMapping();
    verify( packageB, times( 0 ) ).getModuleIdsMapping();

    this.requireJsConfigManager.getContextMapping( this.baseUrl, "/default/base/url/packageB/1.5/index.html" );

    verify( packageA, times( 1 ) ).getModuleIdsMapping();
    verify( packageB, times( 1 ) ).getModuleIdsMapping();

    this.requireJsConfigManager.getContextMapping( this.baseUrl, "/default/base/url/packageA/1.0/index.html" );

    verify( packageA, times( 1 ) ).getModuleIdsMapping();
    verify( packageB, times( 1 ) ).getModuleIdsMapping();

    this.requireJsConfigManager.getContextMapping( this.baseUrl, "/default/base/url/packageB/1.5/index.html" );

    verify( packageA, times( 1 ) ).getModuleIdsMapping();
    verify( packageB, times( 1 ) ).getModuleIdsMapping();
  }

  private IRequireJsPackageConfiguration createRequireJsPackageConfigurationMock(String webRootPath ) {
    IRequireJsPackageConfiguration config = mock( IRequireJsPackageConfiguration.class );
    doReturn( webRootPath ).when( config ).getWebRootPath();

    packageConfigurationMapping = new HashMap<>();
    packageConfigurationMapping.put( "depA", "depA@1.0" );
    packageConfigurationMapping.put( "depA/hi", "depA@1.0/depA/hi" );
    packageConfigurationMapping.put( "depA/hello", "depA@1.0/depA/hello" );

    doReturn( packageConfigurationMapping ).when( config ).getModuleIdsMapping();

    return config;
  }
}
