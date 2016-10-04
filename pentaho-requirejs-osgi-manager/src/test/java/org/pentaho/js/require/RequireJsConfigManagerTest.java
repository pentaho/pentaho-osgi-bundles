/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.js.require;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/15/14.
 */
public class RequireJsConfigManagerTest {
  private Bundle bundle;
  private BundleContext bundleContext;
  private RequireJsConfigManager requireJsConfigManager;
  private Version version;

  @Before
  public void setup() {
    bundle = mock( Bundle.class );
    bundleContext = mock( BundleContext.class );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    when( bundleContext.getBundles() ).thenReturn( new Bundle[] { bundle } );
    requireJsConfigManager = new RequireJsConfigManager();
    requireJsConfigManager.setBundleContext( bundleContext );
    version = mock( Version.class );
    when( version.toString() ).thenReturn( "1.3.1" );
    when( bundle.getVersion() ).thenReturn( version );
    when( bundle.getSymbolicName() ).thenReturn( "angular-bundle" );

  }

  @Test
  public void testSetBundleContext() {
    assertEquals( bundleContext, requireJsConfigManager.getBundleContext() );
  }

  @Test
  public void testInit() throws Exception {
    BundleEvent bundleEvent = mock( BundleEvent.class );
    requireJsConfigManager.init();
  }

  @Test
  public void testSetLastModified() {
    long lastModified = 100L;
    requireJsConfigManager.setLastModified( lastModified );
    assertEquals( lastModified, requireJsConfigManager.getLastModified() );
  }

  @Test
  public void testUpdateBundleContext() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testUpdateBundleContext.json" ) );
    assertTrue( requireJsConfigManager.updateBundleContext( bundle ) );
  }

  @Test
  public void testUpdateBundleContextStopped() throws IOException, ParseException {
    when( bundle.getBundleId() ).thenReturn( 1L );
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testUpdateBundleContextStopped.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    assertTrue( requireJsConfigManager.updateBundleContextStopped( bundle ) );
  }

  @Test
  public void testUpdateBundleContextStopping() throws IOException, ParseException {
    when( bundle.getBundleId() ).thenReturn( 1L );
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testUpdateBundleContextStopped.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    when( bundle.getState() ).thenReturn( Bundle.STOPPING );
    assertTrue( requireJsConfigManager.updateBundleContext( bundle ) );
  }

  @Test
  public void testUpdateBundleContextUninstalled() throws IOException, ParseException {
    when( bundle.getBundleId() ).thenReturn( 1L );
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testUpdateBundleContextStopped.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    when( bundle.getState() ).thenReturn( Bundle.UNINSTALLED );
    assertTrue( requireJsConfigManager.updateBundleContext( bundle ) );
  }

  @Test
  public void testUpdateBundleContextInstalled() throws IOException, ParseException {
    when( bundle.getBundleId() ).thenReturn( 1L );
    when( bundle.getState() ).thenReturn( Bundle.INSTALLED );
    requireJsConfigManager.updateBundleContext( bundle );
    verify( bundle, times( 0 ) ).getResource( anyString() );
  }

  @Test
  /**
   * Calling getResource on an installed bundle will trigger it to resolve, something we don't want to do.
   */
  public void testUpdateBundleContextResolved() throws IOException, ParseException {
    when( bundle.getBundleId() ).thenReturn( 1L );
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testUpdateBundleContextStopped.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    when( bundle.getState() ).thenReturn( Bundle.RESOLVED );
    assertTrue( requireJsConfigManager.updateBundleContext( bundle ) );
  }

  @Test
  public void testBundleChangedNoException() {
    requireJsConfigManager.bundleChanged( bundle );
  }

  @Test
  public void testBundleChangedException() {
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        throw new IOException();
      }
    } );
    requireJsConfigManager.bundleChanged( bundle );
  }

  @Test
  public void testGetRequireJsConfig() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testGetRequireJsConfig.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    requireJsConfigManager.invalidateCache( true );
    String config = requireJsConfigManager.getRequireJsConfig();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }
    RebuildCacheCallableTest.testEquals( JSONValue.parse( new InputStreamReader( this.getClass().getClassLoader()
        .getResourceAsStream( "org/pentaho/js/require/RequireJsConfigManagerTest.testGetRequireJsConfig.json" ) ) ),
      JSONValue.parse( config ) );
  }

  @Test
  /**
   * Ensure that all paths provided in absolute form are converted to relative.
   */
  public void testGetRequireJsConfigRel() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader()
      .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testGetRequireJsConfigRel.abs.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    requireJsConfigManager.invalidateCache( true );
    String config = requireJsConfigManager.getRequireJsConfig();
    if ( config.endsWith( ";" ) ) {
      config = config.substring( 0, config.length() - 1 );
    }

    RebuildCacheCallableTest.testEquals( JSONValue.parse( new InputStreamReader( this.getClass().getClassLoader()
        .getResourceAsStream(
          "org/pentaho/js/require/RequireJsConfigManagerTest.testGetRequireJsConfigRel.rel.json" ) ) ),
      JSONValue.parse( config ) );
  }

  @Test
  public void testExternalResources() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.EXTERNAL_RESOURCES_JSON_PATH ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testExternalResources.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    when( bundle.getResource( "/common-ui/resources/web/common-ui-require-js-cfg.js" ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testExternalResources.internal" ) );
    requireJsConfigManager.invalidateCache( true );
    String config = requireJsConfigManager.getRequireJsConfig();
    assertTrue( config.contains( "internal_test" ) );
  }

  @Test
  public void testStaticResources() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.EXTERNAL_RESOURCES_JSON_PATH ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testExternalStaticResources.json" ) );
    when( bundle.getResource( RequireJsConfigManager.STATIC_RESOURCES_JSON_PATH ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testStaticResources.json" ) );
    requireJsConfigManager.updateBundleContext( bundle );
    when( bundle.getResource( "/testDir/static/common-ui/resources/web/common-ui-require-js-cfg.js" ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testStaticResources.internal" ) );
    requireJsConfigManager.invalidateCache( true );
    String config = requireJsConfigManager.getRequireJsConfig();
    assertTrue( config.contains( "internal_testExternal" ) );
  }

  @Test
  public void testPackages() throws IOException, ParseException {
    when( bundle.getResource( RequireJsConfigManager.PACKAGE_JSON_PATH ) )
      .thenReturn( this.getClass().getClassLoader()
        .getResource( "org/pentaho/js/require/RequireJsConfigManagerTest.testPackages.json" ) );

    requireJsConfigManager.updateBundleContext( bundle );
    requireJsConfigManager.invalidateCache( true );
    String config = requireJsConfigManager.getRequireJsConfig();
    assertTrue( config.contains( "angular-ui-router.stateHelper" ) );
  }

  @Test
  public void testContextRoot() throws IOException, ParseException {
    String[] contextPermutations = new String[] { "fake/root", "/fake/root", "fake/root/" };
    for ( String contextPermutation : contextPermutations ) {
      requireJsConfigManager.setContextRoot( contextPermutation );
      assertEquals( "/fake/root/", requireJsConfigManager.getContextRoot() );
    }
  }

  @Test
  public void testDestroy() throws IOException, ParseException {
    requireJsConfigManager.destroy();
  }
}
