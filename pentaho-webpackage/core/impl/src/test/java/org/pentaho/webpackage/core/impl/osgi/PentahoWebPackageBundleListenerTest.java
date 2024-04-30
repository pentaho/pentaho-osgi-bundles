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
package org.pentaho.webpackage.core.impl.osgi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.core.PentahoWebPackageConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageBundleListenerTest {

  @Test
  public void testBundleChangedShouldRegisterWebpackageServicesOnBundleEventStarted() {
    // arrange
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
    // arrange
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
  public void testRegisterWebPackageServicesShouldReturnOnNullBundle() {
    // arrange
    PentahoWebPackageBundleListener mockWebPackageBundleListener = spy( new PentahoWebPackageBundleListener() );

    // act
    mockWebPackageBundleListener.registerWebPackageServices( null );

    // assert
    verify( mockWebPackageBundleListener, never() ).createWebPackages( any() );
  }

  @Test
  public void testRegisterWebPackageServicesShouldRegisterAWebpackageServiceForEachWebpackageCapabilityProvidedByTheBundle() {
    // arrange
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
  public void testRegisterWebPackageServicesShouldNotTrowIfNoWebpackageCapabilitiesAreProvidedByTheBundle() {
    // arrange
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

  @Test
  public void testUnregisterWebPackageServicesShouldReturnOnNullBundle() {
    // arrange
    PentahoWebPackageBundleListener mockWebPackageBundleListener = spy( new PentahoWebPackageBundleListener() );

    // act
    mockWebPackageBundleListener.unregisterWebPackageServices( null );

    // assert
    verify( mockWebPackageBundleListener, never() ).getBundleServiceRegistrations( anyLong() );
  }

  @Test
  public void testUnregisterWebPackageServicesSouldCallUnregisterServiceOnAllRegisteredServices() {
    // arrange
    int numberOfRegistrations = 3;
    List<ServiceRegistration<IPentahoWebPackage>> webPackages = new ArrayList<>();
    for ( int iRegistrations = 0; iRegistrations < numberOfRegistrations; iRegistrations++ ) {
      webPackages.add( mock( ServiceRegistration.class ) );
    }
    PentahoWebPackageBundleListener mockPackageBundleListener = spy( new PentahoWebPackageBundleListener() );
    Bundle mockBundle = mock( Bundle.class );
    doReturn( webPackages ).when( mockPackageBundleListener ).getBundleServiceRegistrations( anyLong() );

    // act
    mockPackageBundleListener.unregisterWebPackageServices( mockBundle );

    // assert
    for ( ServiceRegistration<IPentahoWebPackage> registration : webPackages ) {
      verify( registration ).unregister();
    }
  }

  @Test
  public void testGetWebPackageCapabilitiesWhenBundleWiringIsNull() {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
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
    Bundle mockBundle = mock( Bundle.class );
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
    Bundle mockBundle = mock( Bundle.class );
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
    String resourceRootPath = "some/resource/path/";

    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    String expectedWebPackageName = "foo";
    String expectedWebPackageVersion = "1.2.3";
    String expectedResourceRootPath = resourceRootPath;
    String mockPackageJson = "{\"name\":\"" + expectedWebPackageName + "\",\"version\":\"" + expectedWebPackageVersion + "\"}";

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "package.json" ) ) )
        .thenReturn( mockUrl );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualWebPackageName = pentahoWebPackage.getName();
    String actualWebPackageVersion = pentahoWebPackage.getVersion();
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();

    // asset
    assertNotNull( "Should not be null", pentahoWebPackage );
    assertEquals( "Should have the correct WebPackage name", expectedWebPackageName, actualWebPackageName );
    assertEquals( "Should have the correct WebPackage version", expectedWebPackageVersion, actualWebPackageVersion );
    assertEquals( "Should have the correct WebPackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  @Test
  public void testNormalizeResourceRootPathNoEndSlash() {
    String resourceRootPath = "some/resource/path";
    String expectedResourceRootPath = "some/resource/path/";

    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    String mockPackageJson = "{\"name\":\"test\",\"version\":\"1.0\"}";

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();

    // asset
    assertEquals( "Should have the correct WebPackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  @Test
  public void testNormalizeResourceRootPathEmpty() {
    String resourceRootPath = "";
    String expectedResourceRootPath = "/";

    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    String mockPackageJson = "{\"name\":\"test\",\"version\":\"1.0\"}";

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( "/package.json" ) ) )
        .thenReturn( mockUrl );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();

    // asset
    assertEquals( "Should have the correct WebPackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  @Test
  public void testNormalizeResourceRootPathMultipleSlashes() {
    String resourceRootPath = "////";
    String expectedResourceRootPath = "/";

    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    String mockPackageJson = "{\"name\":\"test\",\"version\":\"1.0\"}";

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( "/package.json" ) ) )
        .thenReturn( mockUrl );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();

    // asset
    assertEquals( "Should have the correct WebPackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  @Test
  public void testNormalizeResourceRootPathMultipleEndSlashes() {
    String resourceRootPath = "some/resource/path///";
    String expectedResourceRootPath = "some/resource/path/";

    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();

    String mockPackageJson = "{\"name\":\"test\",\"version\":\"1.0\"}";

    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( expectedResourceRootPath + "package.json" ) ) )
        .thenReturn( mockUrl );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );
    String actualResourceRootPath = pentahoWebPackage.getResourceRootPath();

    // asset
    assertEquals( "Should have the correct WebPackage Resource root path", expectedResourceRootPath, actualResourceRootPath );
  }

  private URL createMockUrlConnection( String payload ) {
    URLConnection mockUrlCon = mock( URLConnection.class );
    URLStreamHandler stubUrlHandler = null;
    try {
      stubUrlHandler = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
          return mockUrlCon;
        }
      };
      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( payload.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }

  @Test
  public void testCreateWebPackageShouldReturnNullGivenInvalidPackageJsonUrl() {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    when( mockBundle.getResource( anyString() ) )
        .thenReturn( null );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );

    // assert
    assertNull( pentahoWebPackage );
  }

  @Test
  public void testCreateWebPackageShouldReturnNullGivenExceptionThrown() {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    doThrow( new RuntimeException( "Error" ) ).when( mockBundle ).getResource( anyString() );

    // act
    IPentahoWebPackage pentahoWebPackage = listener.createWebPackage( mockBundle, mockBundleCapability );

    // assert
    assertNull( pentahoWebPackage );
  }
}