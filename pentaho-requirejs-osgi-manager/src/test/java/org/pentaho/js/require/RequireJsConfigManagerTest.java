/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequireJsConfigManagerTest {
  private String baseUrl;

  private Bundle mockContextBundle;
  private Bundle[] mockBundles;

  private BundleContext mockBundleContext;

  private RequireJsConfigManager requireJsConfigManager;
  private long mockBundleCounter;

  @Before
  public void setup() throws Exception {
    this.baseUrl = "/default/base/url/";

    this.mockContextBundle = mock( Bundle.class );

    this.mockBundleCounter = 1L;

    this.mockBundles = new Bundle[5];

    this.mockBundles[0] = this.createMockBundle( "non-client-side-bundle", "0.1", Bundle.ACTIVE );
    this.mockBundles[1] = this.createMockPackageJsonBundle( "lib1", "1.0", Bundle.ACTIVE );
    this.mockBundles[2] = this.createMockRequireJsonBundle( "lib2", "2.0", Bundle.ACTIVE );
    this.mockBundles[3] = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );
    this.mockBundles[4] = this.createMockExternalStaticResourcesBundle( "lib4", "4.0", Bundle.ACTIVE );

    this.mockBundleContext = mock( BundleContext.class );
    when( this.mockBundleContext.getBundle() ).thenReturn( this.mockContextBundle );
    when( this.mockBundleContext.getBundles() ).thenReturn( this.mockBundles );

    this.requireJsConfigManager = spy( new RequireJsConfigManager() );
    this.requireJsConfigManager.setBundleContext( this.mockBundleContext );

    this.requireJsConfigManager.init();
  }

  @Test
  public void testInit() throws Exception {
    // init already called above at setup()

    // check that a bundle listener is registered
    // note: any( RequireJsBundleListener.class ) in reality don't do type checking in mockito 1.9
    verify( this.mockBundleContext, times( 1 ) ).addBundleListener( any( RequireJsBundleListener.class ) );

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );

    // check if the already registered bundles configurations appear in the require configuration
    assertTrue( config.contains( "lib1_1.0" ) );
    assertTrue( config.contains( "lib2" ) );
    assertTrue( config.contains( "/* Following configurations are from bundle [" + this.mockBundles[3].getBundleId() + "] - lib3:3.0*/" ) );
    assertTrue( config.contains( "var lib3 = \"lib3: some external code!\";" ) );
    assertTrue( config.contains( "/* End of bundle [" + this.mockBundles[3].getBundleId() + "] - lib3:3.0*/" ) );
    assertTrue( config.contains( "/* Following configurations are from bundle [" + this.mockBundles[4].getBundleId() + "] - lib4:4.0*/" ) );
    assertTrue( config.contains( "var lib4 = \"lib4: some external static code!\";" ) );
    assertTrue( config.contains( "var lib4 = \"lib4: some external code!\";" ) );
    assertTrue( config.contains( "/* End of bundle [" + this.mockBundles[4].getBundleId() + "] - lib4:4.0*/" ) );
  }

  @Test( expected = Exception.class )
  public void testReInit() throws Exception {
    this.requireJsConfigManager.init();
  }

  @Test
  public void testDestroy() throws IOException, ParseException {
    this.requireJsConfigManager.destroy();

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    // check that a bundle listener is unregistered
    // note: any( RequireJsBundleListener.class ) in reality don't do type checking in mockito 1.9
    verify( this.mockBundleContext, times( 1 ) ).removeBundleListener( any( RequireJsBundleListener.class ) );
  }

  @Test
  public void testBundleChangedException() {
    Bundle mockBundle = this.createMockBundle( "some-mockBundle", "2.0.0", Bundle.ACTIVE );

    when( mockBundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenAnswer( invocation -> {
      throw new IOException();
    } );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testBundleChangedNewNonClientBundle() throws IOException, ParseException {
    Bundle mockBundle = this.createMockBundle( "another-non-client-side-bundle", "2.0.0", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is not called
    verify( this.requireJsConfigManager, times( 0 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testBundleChangedNewPackageJsonBundle() throws IOException, ParseException {
    Bundle mockBundle = this.createMockPackageJsonBundle( "lib5", "1.5", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    // dirty quick check if the lib is in the require configuration
    // proper testing is done on the pentaho-requirejs-utils module
    assertTrue( config.contains( "lib5_1.5" ) );
  }

  @Test
  public void testBundleChangedNewRequireJsonBundle() throws IOException, ParseException {
    Bundle mockBundle = this.createMockRequireJsonBundle( "lib6", "2.0.0", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    // dirty quick check if the lib is in the require configuration
    // proper testing is done on the pentaho-requirejs-utils module
    assertTrue( config.contains( "lib6" ) );
  }

  @Test
  public void testBundleChangedNewExternalResourcesBundle() throws IOException, ParseException {
    Bundle mockBundle = this.createMockExternalResourcesBundle( "lib7", "7.0", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    assertTrue( config.contains( "/* Following configurations are from bundle [" + mockBundle.getBundleId() + "] - lib7:7.0*/" ) );
    assertTrue( config.contains( "var lib7 = \"lib7: some external code!\";" ) );
    assertTrue( config.contains( "/* End of bundle [" + mockBundle.getBundleId() + "] - lib7:7.0*/" ) );
  }

  @Test
  public void testBundleChangedNewExternalStaticResourcesBundle() throws IOException, ParseException {
    Bundle mockBundle = this.createMockExternalStaticResourcesBundle( "lib8", "8.0", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    assertTrue( config.contains( "/* Following configurations are from bundle [" + mockBundle.getBundleId() + "] - lib8:8.0*/" ) );
    assertTrue( config.contains( "var lib8 = \"lib8: some external static code!\";" ) );
    assertTrue( config.contains( "var lib8 = \"lib8: some external code!\";" ) );
    assertTrue( config.contains( "/* End of bundle [" + mockBundle.getBundleId() + "] - lib8:8.0*/" ) );
  }

  @Test
  public void testBundleChangedExistingNonClientBundle() throws IOException, ParseException {
    Bundle mockBundle = this.mockBundles[0];
    when( mockBundle.getState() ).thenReturn( Bundle.STOPPING );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is not called
    verify( this.requireJsConfigManager, times( 0 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testBundleChangedExistingPackageJsonBundle() throws IOException, ParseException {
    Bundle mockBundle = this.mockBundles[1];
    when( mockBundle.getState() ).thenReturn( Bundle.STOPPING );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    // dirty quick check if the lib isn't anymore in the require configuration
    // proper testing is done on the pentaho-requirejs-utils module
    assertFalse( config.contains( "lib1_1.0" ) );
  }

  @Test
  public void testBundleChangedExistingRequireJsonBundle() throws IOException, ParseException {
    Bundle mockBundle = this.mockBundles[2];
    when( mockBundle.getState() ).thenReturn( Bundle.STOPPING );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    // dirty quick check if the lib isn't anymore in the require configuration
    // proper testing is done on the pentaho-requirejs-utils module
    assertFalse( config.contains( "lib2" ) );
  }

  @Test
  public void testBundleChangedExistingExternalResourcesBundle() throws IOException, ParseException {
    Bundle mockBundle = this.mockBundles[3];
    when( mockBundle.getState() ).thenReturn( Bundle.STOPPING );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    assertFalse( config.contains( "/* Following configurations are from bundle [" + mockBundle.getBundleId() + "] - lib3:3.0*/" ) );
    assertFalse( config.contains( "var lib3 = \"lib3: some external code!\";" ) );
    assertFalse( config.contains( "/* End of bundle [" + mockBundle.getBundleId() + "] - lib3:3.0*/" ) );
  }

  @Test
  public void testBundleChangedExistingExternalStaticResourcesBundle() throws IOException, ParseException {
    Bundle mockBundle = this.mockBundles[4];
    when( mockBundle.getState() ).thenReturn( Bundle.STOPPING );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );
    assertFalse( config.contains( "/* Following configurations are from bundle [" + mockBundle.getBundleId() + "] - lib4:4.0*/" ) );
    assertFalse( config.contains( "var lib4 = \"lib4: some external static code!\";" ) );
    assertFalse( config.contains( "var lib4 = \"lib4: some external code!\";" ) );
    assertFalse( config.contains( "/* End of bundle [" + mockBundle.getBundleId() + "] - lib4:4.0*/" ) );
  }

  @Test
  public void testUpdateBundleContextInstalled() throws IOException, ParseException {
    Bundle mockBundle = this.createMockBundle( "some-mockBundle", "2.0.0", Bundle.INSTALLED );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // calling getResource on an installed mockBundle will trigger it to resolve, something we don't want to do
    // should not try to do anything at INSTALLED state
    verify( mockBundle, times( 0 ) ).getResource( anyString() );

    // check that invalidateCachedConfigurations is not called
    verify( this.requireJsConfigManager, times( 0 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfigTimeout() throws IOException, ParseException, ExecutionException, InterruptedException {
    Future mockFuture = mock( Future.class );
    doThrow( InterruptedException.class ).when( mockFuture ).get();
    doReturn( mockFuture ).when( this.requireJsConfigManager ).getCachedConfiguration( this.baseUrl );

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );

    assertTrue( config.contains( "Error computing RequireJS Config" ) );

    // check that invalidateCachedConfigurations is not called
    verify( this.requireJsConfigManager, times( 0 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetRequireJsConfigException() throws IOException, ParseException, ExecutionException, InterruptedException {
    Future<String> mockFuture = mock( Future.class );
    doThrow( ExecutionException.class ).when( mockFuture ).get();
    doReturn( mockFuture ).when( this.requireJsConfigManager ).getCachedConfiguration( this.baseUrl );

    String config = this.requireJsConfigManager.getRequireJsConfig( this.baseUrl );

    assertTrue( config.contains( "Error computing RequireJS Config" ) );

    // check that invalidateCachedConfigurations is called
    verify( this.requireJsConfigManager, atLeastOnce() ).invalidateCachedConfigurations();
  }

  @Test
  public void testGetLastModified() {
    long lastModified = this.requireJsConfigManager.getLastModified();

    Bundle mockBundle = this.createMockBundle( "another-non-client-side-bundle", "2.0.0", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // new bundle doesn't change configurations, so the timestamp should remain the same
    assertEquals( lastModified, this.requireJsConfigManager.getLastModified() );

    mockBundle = this.createMockPackageJsonBundle( "lib5", "1.5", Bundle.ACTIVE );

    this.requireJsConfigManager.bundleChanged( mockBundle );

    // new bundle does change configurations, so the timestamp should change
    assertNotEquals( lastModified, this.requireJsConfigManager.getLastModified() );
  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );

    when( mockBundle.getBundleId() ).thenReturn( ++this.mockBundleCounter );

    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );

    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );

    when( mockBundle.getState() ).thenReturn( bundleState );

    return mockBundle;
  }

  private Bundle createMockPackageJsonBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsConfigManager.PACKAGE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-package.json" ) );

    return mockBundle;
  }

  private Bundle createMockRequireJsonBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsConfigManager.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-require.json" ) );

    return mockBundle;
  }

  private Bundle createMockExternalResourcesBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsConfigManager.EXTERNAL_RESOURCES_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-external-resources.json" ) );

    when( mockBundle.getResource( "/resources/external/" + bundleName + "-require-js-cfg.js" ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-external-resources-script.js" ) );

    return mockBundle;
  }

  private Bundle createMockExternalStaticResourcesBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockExternalResourcesBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsConfigManager.STATIC_RESOURCES_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-static-resources.json" ) );

    when( mockBundle.getResource( "/resources/static/" + bundleName + "-require-js-cfg.js" ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/js/require/" + bundleName + "-static-resources-script.js" ) );

    return mockBundle;
  }
}
