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
package org.pentaho.requirejs.impl.listeners;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.requirejs.IPlatformPluginRequireJsConfigurations;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.impl.RequireJsConfigManager;
import org.pentaho.requirejs.impl.types.MetaInfPackageJson;
import org.pentaho.requirejs.impl.types.MetaInfRequireJson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RequireJsBundleListenerTest {
  private RequireJsBundleListener requireJsBundleListener;

  private BundleContext mockBundleContext;
  private ArrayList<BundleContext> mockBundleMockContexts;
  private ArrayList<ServiceRegistration<?>> mockServiceRegistrations;

  private RequireJsConfigManager mockRequireJsConfigManager;

  private int mockBundleCounter;

  @Before
  public void setup() {
    this.mockBundleCounter = 0;

    this.mockBundleContext = mock( BundleContext.class );

    mockBundleMockContexts = new ArrayList<>();
    mockServiceRegistrations = new ArrayList<>();

    Bundle[] mockBundles = new Bundle[ 0 ];

    doReturn( mockBundles ).when( this.mockBundleContext ).getBundles();

    this.mockRequireJsConfigManager = mock( RequireJsConfigManager.class );

    this.requireJsBundleListener = new RequireJsBundleListener();

    this.requireJsBundleListener.setBundleContext( this.mockBundleContext );
    this.requireJsBundleListener.setRequireJsConfigManager( this.mockRequireJsConfigManager );

    this.requireJsBundleListener.init();
  }

  @Test
  public void init() {
    Bundle mockBundleNoClientSide = this.createMockBundle( "non-client-side-bundle", "0.1", Bundle.ACTIVE );
    Bundle mockBundleWithPackageJson = this.createMockPackageJsonBundle( "lib1", "1.0", Bundle.ACTIVE );
    Bundle mockBundleWithRequireJson = this.createMockRequireJsonBundle( "lib2", "2.0", Bundle.ACTIVE );
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );
    Bundle mockBundleWithExternalAndStaticResources = this.createMockExternalStaticResourcesBundle( "lib4", "4.0", Bundle.ACTIVE );

    Bundle[] mockBundles = new Bundle[ 5 ];
    mockBundles[ 0 ] = mockBundleNoClientSide;
    mockBundles[ 1 ] = mockBundleWithPackageJson;
    mockBundles[ 2 ] = mockBundleWithRequireJson;
    mockBundles[ 3 ] = mockBundleWithExternalResources;
    mockBundles[ 4 ] = mockBundleWithExternalAndStaticResources;

    doReturn( mockBundles ).when( this.mockBundleContext ).getBundles();

    RequireJsBundleListener spyed = spy( new RequireJsBundleListener() );

    spyed.setBundleContext( this.mockBundleContext );
    spyed.setRequireJsConfigManager( this.mockRequireJsConfigManager );

    spyed.init();

    // check that the bundle listener is registered
    verify( this.mockBundleContext, times( 1 ) ).addBundleListener( same( this.requireJsBundleListener ) );

    // check it adds already started bundles
    verify( spyed, times( 5 ) ).addBundle( any() );
  }

  @Test
  public void destroy() {
    this.requireJsBundleListener.destroy();

    // check that a bundle listener is unregistered
    verify( this.mockBundleContext, times( 1 ) ).removeBundleListener( same( this.requireJsBundleListener ) );
  }

  @Test
  public void bundleStarted() {
    RequireJsBundleListener spyed = spy( this.requireJsBundleListener );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.ACTIVE );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.STARTED );

    spyed.bundleChanged( event );

    verify( spyed, times( 1 ) ).addBundle( bundle );
    verify( spyed, times( 0 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleStopped() {
    RequireJsBundleListener spyed = spy( this.requireJsBundleListener );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.RESOLVED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.STOPPED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleUnresolved() {
    RequireJsBundleListener spyed = spy( this.requireJsBundleListener );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.INSTALLED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.UNRESOLVED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleOther() {
    RequireJsBundleListener spyed = spy( this.requireJsBundleListener );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.RESOLVED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.RESOLVED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 0 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleUninstalled() {
    RequireJsBundleListener spyed = spy( this.requireJsBundleListener );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.UNINSTALLED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.UNINSTALLED );

    spyed.bundleChanged( event );

    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleStartedWithExternalResources() {
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );
    BundleEvent event = this.createMockBundleEvent( mockBundleWithExternalResources, BundleEvent.STARTED );

    this.requireJsBundleListener.bundleChanged( event );

    verify( this.mockRequireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void bundleStoppedWithExternalResources() {
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithExternalResources );

    BundleEvent event = this.createMockBundleEvent( mockBundleWithExternalResources, BundleEvent.STOPPED );

    this.requireJsBundleListener.bundleChanged( event );

    verify( this.mockRequireJsConfigManager, times( 1 ) ).invalidateCachedConfigurations();
  }

  @Test
  public void testAddBundleNotActive() {
    Bundle mockBundle = this.createMockPackageJsonBundle( "some-mockBundle", "2.0.0", Bundle.INSTALLED );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleException() {
    Bundle mockBundle = this.createMockBundle( "some-mockBundle", "2.0.0", Bundle.ACTIVE );

    doThrow( new RuntimeException() ).when( mockBundle ).getResource( RequireJsBundleListener.REQUIRE_JSON_PATH );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is true
    assertTrue( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 0 ) ).registerService(
      eq( IRequireJsPackage.class ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleNewNonClientBundle() {
    Bundle mockBundle = this.createMockBundle( "another-non-client-side-bundle", "2.0.0", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleNewPackageJsonBundle() {
    Bundle mockBundle = this.createMockPackageJsonBundle( "lib5", "1.5", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 1 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( MetaInfPackageJson.class ), any() );
  }

  @Test
  public void testAddBundleNewRequireJsonBundle() {
    Bundle mockBundle = this.createMockRequireJsonBundle( "lib6", "2.0.0", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 1 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( MetaInfRequireJson.class ), any() );
  }

  @Test
  public void testAddBundleNewExternalResourcesBundle() {
    Bundle mockBundle = this.createMockExternalResourcesBundle( "lib7", "7.0", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is true
    assertTrue( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleNewExternalStaticResourcesBundle() {
    Bundle mockBundle = this.createMockExternalStaticResourcesBundle( "lib8", "8.0", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundle );

    // check that shouldInvalidate is true
    assertTrue( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundle.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleExistingNonClientBundle() {
    Bundle mockBundleNoClientSide = this.createMockBundle( "non-client-side-bundle", "0.1", Bundle.ACTIVE );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundleNoClientSide );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockBundleMockContexts.get( (int) mockBundleNoClientSide.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleExistingPackageJsonBundle() {
    Bundle mockBundleWithPackageJson = this.createMockPackageJsonBundle( "lib1", "1.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithPackageJson );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundleWithPackageJson );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockServiceRegistrations.get( (int) mockBundleWithPackageJson.getBundleId() ), times( 1 ) ).unregister();

    verify( mockBundleMockContexts.get( (int) mockBundleWithPackageJson.getBundleId() ), times( 2 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( MetaInfPackageJson.class ), any() );
  }

  @Test
  public void testAddBundleExistingRequireJsonBundle() {
    Bundle mockBundleWithRequireJson = this.createMockRequireJsonBundle( "lib2", "2.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithRequireJson );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundleWithRequireJson );

    // check that shouldInvalidate is false
    assertFalse( shouldInvalidate );

    verify( mockServiceRegistrations.get( (int) mockBundleWithRequireJson.getBundleId() ), times( 1 ) ).unregister();

    verify( mockBundleMockContexts.get( (int) mockBundleWithRequireJson.getBundleId() ), times( 2 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( MetaInfRequireJson.class ), any() );
  }

  @Test
  public void testAddBundleExistingExternalResourcesBundle() {
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithExternalResources );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundleWithExternalResources );

    // check that shouldInvalidate is true
    assertTrue( shouldInvalidate );

    // TODO Not unregistered

    verify( mockBundleMockContexts.get( (int) mockBundleWithExternalResources.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void testAddBundleExistingExternalStaticResourcesBundle() {
    Bundle mockBundleWithExternalAndStaticResources = this.createMockExternalStaticResourcesBundle( "lib4", "4.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithExternalAndStaticResources );

    boolean shouldInvalidate = this.requireJsBundleListener.addBundle( mockBundleWithExternalAndStaticResources );

    // check that shouldInvalidate is true
    assertTrue( shouldInvalidate );

    // TODO Not unregistered

    verify( mockBundleMockContexts.get( (int) mockBundleWithExternalAndStaticResources.getBundleId() ), times( 0 ) ).registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() );
  }

  @Test
  public void getScriptsEmpty() {
    assertTrue( this.requireJsBundleListener.getScripts().isEmpty() );
  }

  @Test
  public void getScriptsExternalResourcesBundle() {
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithExternalResources );

    Collection<IPlatformPluginRequireJsConfigurations> scripts = this.requireJsBundleListener.getScripts();

    assertEquals( 1, scripts.size() );

    List<URL> requireConfigurations = scripts.iterator().next().getRequireConfigurationsURLs();
    assertEquals( 1, requireConfigurations.size() );
  }

  @Test
  public void getScriptsExternalStaticResourcesBundle() {
    Bundle mockBundleWithExternalAndStaticResources = this.createMockExternalStaticResourcesBundle( "lib4", "4.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleWithExternalAndStaticResources );

    Collection<IPlatformPluginRequireJsConfigurations> scripts = this.requireJsBundleListener.getScripts();

    assertEquals( 1, scripts.size() );

    List<URL> requireConfigurations = scripts.iterator().next().getRequireConfigurationsURLs();
    assertEquals( 2, requireConfigurations.size() );
  }

  @Test
  public void getScriptsMany() {
    Bundle mockBundleNoClientSide = this.createMockBundle( "non-client-side-bundle", "0.1", Bundle.ACTIVE );
    Bundle mockBundleWithPackageJson = this.createMockPackageJsonBundle( "lib1", "1.0", Bundle.ACTIVE );
    Bundle mockBundleWithRequireJson = this.createMockRequireJsonBundle( "lib2", "2.0", Bundle.ACTIVE );
    Bundle mockBundleWithExternalResources = this.createMockExternalResourcesBundle( "lib3", "3.0", Bundle.ACTIVE );
    Bundle mockBundleWithExternalAndStaticResources = this.createMockExternalStaticResourcesBundle( "lib4", "4.0", Bundle.ACTIVE );

    this.requireJsBundleListener.addBundle( mockBundleNoClientSide );
    this.requireJsBundleListener.addBundle( mockBundleWithPackageJson );
    this.requireJsBundleListener.addBundle( mockBundleWithRequireJson );
    this.requireJsBundleListener.addBundle( mockBundleWithExternalResources );
    this.requireJsBundleListener.addBundle( mockBundleWithExternalAndStaticResources );

    Collection<IPlatformPluginRequireJsConfigurations> scripts = this.requireJsBundleListener.getScripts();

    assertEquals( 2, scripts.size() );
  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );

    when( mockBundle.getBundleId() ).thenReturn( (long) this.mockBundleCounter++ );

    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );

    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );

    when( mockBundle.getState() ).thenReturn( bundleState );

    BundleContext mockBundleMockContext = mock( BundleContext.class );
    final ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );
    when( mockBundleMockContext
        .registerService( eq( IRequireJsPackage.class.getName() ), any( IRequireJsPackage.class ), any() ) )
        .thenReturn( mockServiceRegistration );

    doReturn( mockBundleMockContext ).when( mockBundle ).getBundleContext();

    mockBundleMockContexts.add( mockBundleMockContext );
    mockServiceRegistrations.add( mockServiceRegistration );

    return mockBundle;
  }

  private Bundle createMockPackageJsonBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsBundleListener.PACKAGE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-package.json" ) );

    return mockBundle;
  }

  private Bundle createMockRequireJsonBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsBundleListener.REQUIRE_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-require.json" ) );

    return mockBundle;
  }

  private Bundle createMockExternalResourcesBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsBundleListener.EXTERNAL_RESOURCES_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-external-resources.json" ) );

    when( mockBundle.getResource( "/resources/external/" + bundleName + "-require-js-cfg.js" ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-external-resources-script.js" ) );

    return mockBundle;
  }

  private Bundle createMockExternalStaticResourcesBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = this.createMockExternalResourcesBundle( bundleName, bundleVersion, bundleState );

    when( mockBundle.getResource( RequireJsBundleListener.STATIC_RESOURCES_JSON_PATH ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-static-resources.json" ) );

    when( mockBundle.getResource( "/resources/static/" + bundleName + "-require-js-cfg.js" ) ).thenReturn( this.getClass().getClassLoader().getResource( "org/pentaho/requirejs/impl/" + bundleName + "-static-resources-script.js" ) );

    return mockBundle;
  }

  private BundleEvent createMockBundleEvent( Bundle bundle, int status ) {
    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    when( mockBundleEvent.getBundle() ).thenReturn( bundle );
    when( mockBundleEvent.getType() ).thenReturn( status );

    return mockBundleEvent;
  }
}