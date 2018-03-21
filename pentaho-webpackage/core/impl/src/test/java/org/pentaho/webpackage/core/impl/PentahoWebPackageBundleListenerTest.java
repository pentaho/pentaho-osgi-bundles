/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.core.impl;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.core.PentahoWebPackageConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageBundleListenerTest {

  private String resourceRootPath = "some/resource/path";

  @Test
  public void testBundleChangedShouldRegisterWebpackageServicesOnBundleEventStarted() {
    PentahoWebPackageBundleListener bundleListener = spy( new PentahoWebPackageBundleListener() );
    Bundle bundle = mock( Bundle.class );
    BundleEvent bundleEvent = createBundleEventMock( BundleEvent.STARTED, bundle );

    // act
    bundleListener.bundleChanged( bundleEvent );

    // assert
    verify( bundleListener ).registerWebPackageServices( bundle );
  }

  @Test
  public void testBundleChangedShouldUnregisterWebpackageServicesOnBundleEventUninstalled() {
    this.testBundleChangedShouldUnregisterWebpackageServicesOnBundleEvent( BundleEvent.UNINSTALLED );
  }

  @Test
  public void testBundleChangedShouldUnregisterWebpackageServicesOnBundleEventUnresolved() {
    this.testBundleChangedShouldUnregisterWebpackageServicesOnBundleEvent( BundleEvent.UNRESOLVED );
  }

  @Test
  public void testBundleChangedShouldUnregisterWebpackageServicesOnBundleEventStopped() {
    this.testBundleChangedShouldUnregisterWebpackageServicesOnBundleEvent( BundleEvent.STOPPED );
  }

  private void testBundleChangedShouldUnregisterWebpackageServicesOnBundleEvent( int bundleEventType ) {
    PentahoWebPackageBundleListener bundleListener = spy( new PentahoWebPackageBundleListener() );
    Bundle bundle = mock( Bundle.class );
    BundleEvent bundleEvent = createBundleEventMock( bundleEventType, bundle );

    // act
    bundleListener.bundleChanged( bundleEvent );

    // assert
    verify( bundleListener ).unregisterWebPackageServices( bundle );
  }

  private BundleEvent createBundleEventMock( int eventType, Bundle bundle ) {
    BundleEvent bundleEvent = mock( BundleEvent.class );
    doReturn( eventType ).when( bundleEvent ).getType();
    doReturn( bundle ).when( bundleEvent ).getBundle();
    return bundleEvent;
  }

  @Test
  public void testRegisterShouldRegisterAWebpackageServiceForEachWebpackageCapabilityProvidedByTheBundle() {
    PentahoWebPackageBundleListener bundleListener = spy( new PentahoWebPackageBundleListener() );
    Bundle bundle = mock( Bundle.class );
    BundleContext bundleContext = mock( BundleContext.class );
    doReturn( bundleContext ).when( bundle ).getBundleContext();

    int numberOfWebpackages = 3;
    List<IPentahoWebPackage> webPackages = new ArrayList<>();
    IPentahoWebPackage expectedPentahoWebpackage = mock( IPentahoWebPackage.class );
    for ( int i = 0; i < numberOfWebpackages; i++ ) {
      webPackages.add( expectedPentahoWebpackage );
    }
    doReturn( webPackages.stream() ).when( bundleListener ).createWebPackages( bundle );

    // act
    bundleListener.registerWebPackageServices( bundle );

    // assert
    verify( bundleContext, times( numberOfWebpackages ) ).registerService( IPentahoWebPackage.class, expectedPentahoWebpackage, null );
  }

  @Test
  public void testRegisterShouldNotTrowIfNoWebpackageCapabilitiesAreProvidedByTheBundle() {
    PentahoWebPackageBundleListener bundleListener = spy( new PentahoWebPackageBundleListener() );
    Bundle bundle = mock( Bundle.class );
    BundleContext bundleContext = mock( BundleContext.class );
    doReturn( bundleContext ).when( bundle ).getBundleContext();

    doReturn( Stream.empty() ).when( bundleListener ).createWebPackages( bundle );

    // act
    bundleListener.registerWebPackageServices( bundle );

    // assert
    verify( bundleContext, never() ).registerService( any( String.class ), any(), any() );
  }


  // Also tests registerWebPackageServices
  @Test
  public void testBundleChangedShouldRegisterWebpackageServicesOnBundleEventStartedTTT() {
    // arrange
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    bundleCapabilityList.add( mockBundleCapability );
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    doReturn( mockBundle ).when( mockBundleEvent ).getBundle();
    when( mockBundleEvent.getType() ).thenReturn( BundleEvent.STARTED );

    int expectedBundleWebPackageServiceReferencesSize = 1;

    // act
    listener.bundleChanged( mockBundleEvent );
    int actualBundleWebPackageServiceReferencesSize = listener.bundleWebPackageServiceRegistrations.size();

    // assert
    assertEquals( "Should return a collection with one element", expectedBundleWebPackageServiceReferencesSize, actualBundleWebPackageServiceReferencesSize );
  }

  // Also tests unregisterWebPackageServices
  @Test
  public void testBundleChangedShouldRegisterWebpackageServicesOnBundleEventUninstalledOrUnresolvedOrStoppedTTT() {
    // arrange
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    bundleCapabilityList.add( mockBundleCapability );
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    doReturn( mockBundle ).when( mockBundleEvent ).getBundle();
    when( mockBundleEvent.getType() ).thenReturn( BundleEvent.STARTED );

    int expectedBundleWebPackageServiceReferencesSizeAfterStarted = 1;
    int expectedBundleWebPackageServiceReferencesSizeAfterUninstalled = 0;

    // act
    listener.bundleChanged( mockBundleEvent );
    int actualBundleWebPackageServiceReferencesSizeAfterStarted = listener.bundleWebPackageServiceRegistrations.size();

    when( mockBundleEvent.getType() ).thenReturn( BundleEvent.UNINSTALLED );
    listener.bundleChanged( mockBundleEvent );

    int actualBundleWebPackageServiceReferencesSizeAfterUninstalled = listener.bundleWebPackageServiceRegistrations.size();

    // assert
    assertEquals( "Should return a collection with one element", expectedBundleWebPackageServiceReferencesSizeAfterStarted, actualBundleWebPackageServiceReferencesSizeAfterStarted );
    assertEquals( "Should return a collection with one element", expectedBundleWebPackageServiceReferencesSizeAfterUninstalled, actualBundleWebPackageServiceReferencesSizeAfterUninstalled );
  }

  // this test is just for coverage
  @Test
  public void testRegisterWebPackageServicesWhenBundleIsNull() {
    // arrange
    PentahoWebPackageBundleListener listener = spy( new PentahoWebPackageBundleListener() );

    // act
    listener.registerWebPackageServices( null );

    // assert

  }

  // This test is just for coverage
  @Test
  public void testUnregisterWebPackageServicesWhenBundleIsNull() {
    // arrange
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    // act
    listener.unregisterWebPackageServices( null );

    // assert
  }

  @Test
  public void testGetWebPackageCapabilitiesWhenBundleWiringIsNull() {
    // arrange
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    int expectedCapabilitiesSize = 0;

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( null );

    // act
    int actualCapabilitiesSize = listener.getWebPackageCapabilities( mockBundle ).size();

    // assert
    assertEquals( expectedCapabilitiesSize, actualCapabilitiesSize );
  }

  @Test
  public void testGetWebPackageCapabilitiesWhenBundleWiringHasNoCapabilities() {
    // arrange
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    int expectedCapabilitiesSize = 0;

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    // act
    int actualCapabilitiesSize = listener.getWebPackageCapabilities( mockBundle ).size();

    // assert
    assertEquals( expectedCapabilitiesSize, actualCapabilitiesSize );
  }

  @Test
  public void testGetWebPackageCapabilitiesWhenBundleWiringHasCapabilities() {
    // arrange
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    bundleCapabilityList.add( mockBundleCapability );
    int expectedCapabilitiesSize = 1;

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    // act
    int actualCapabilitiesSize = listener.getWebPackageCapabilities( mockBundle ).size();

    // assert
    assertEquals( expectedCapabilitiesSize, actualCapabilitiesSize );
  }

  @Test
  public void testCreateWebPackageShouldReturnValidPentahoWebPackage() {
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    String expectedWebPackageName = "foo";
    String expectedWebPackageVersion = "1.2.3";
    String expectedResourceRootPath = resourceRootPath;

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualWebPackageName = pentahoWebPackage.getName();
    String actualWebPackageVersion = pentahoWebPackage.getVersion();
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();


    // asset
    assertNotNull( "Should nor be null", pentahoWebPackage );
    assertEquals( "Should have the correct webpackage name", expectedWebPackageName, actualWebPackageName );
    assertEquals( "Should have the correct webpackage version", expectedWebPackageVersion, actualWebPackageVersion );
    assertEquals( "Should have the correct webpackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  @Test
  public void testCreateWebPackageShouldReturnNullGivenInvalidPackageJsonUrl() {
    // arrange
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( null );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );

    // assert
    assertNull( pentahoWebPackage );
  }

  @Test
  public void testCreateWebPackageShouldReturnNullGivenExceptionThrown() {
    // arrange
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    doThrow( new RuntimeException( "Error" ) ).when( mockBundle ).getResource( any() );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );

    // assert
    assertNull( pentahoWebPackage );
  }

  @Test
  public void testGetRootShouldReturnCapabilityRootPath() {
    // arrange
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    String expectedRootPath = resourceRootPath;

    // act
    String actualRootPath = listener.getRoot( mockBundleCapability );

    // assert
    assertEquals( "Should return capability root path", expectedRootPath, actualRootPath );
  }
}