/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.extender.http.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.PentahoWebPackageService;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageServiceImplTest {
  private long mockBundleCounter;

  private PentahoWebPackageServiceImpl service;
  private Map<String, ServiceRegistration> serviceRegistrationMap;

  @Before
  public void setup() {
    this.mockBundleCounter = 1L;

    this.service = new PentahoWebPackageServiceImpl();
    this.serviceRegistrationMap = new HashMap<>();
  }

  @Test
  public void bundleStarted() {
    PentahoWebPackageServiceImpl spyed = spy( this.service );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.ACTIVE );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.STARTED );

    spyed.bundleChanged( event );

    verify( spyed, times( 1 ) ).addBundle( bundle );
    verify( spyed, times( 0 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleStopped() {
    PentahoWebPackageServiceImpl spyed = spy( this.service );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.RESOLVED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.STOPPED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleUnresolved() {
    PentahoWebPackageServiceImpl spyed = spy( this.service );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.INSTALLED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.UNRESOLVED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleOther() {
    PentahoWebPackageServiceImpl spyed = spy( this.service );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.RESOLVED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.RESOLVED );

    spyed.bundleChanged( event );

    verify( spyed, times( 0 ) ).addBundle( bundle );
    verify( spyed, times( 0 ) ).removeBundle( bundle );
  }

  @Test
  public void bundleUninstalled() {
    PentahoWebPackageServiceImpl spyed = spy( this.service );

    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.UNINSTALLED );
    BundleEvent event = this.createMockBundleEvent( bundle, BundleEvent.UNINSTALLED );

    spyed.bundleChanged( event );

    verify( spyed, times( 1 ) ).removeBundle( bundle );
  }

  @Test
  public void addWebPackageBundle() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1b" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1c" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    ArgumentCaptor<ResourceMapping> resourceMappingCaptor = ArgumentCaptor.forClass( ResourceMapping.class );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 3 ) ).registerService( eq( ResourceMapping.class.getName() ), resourceMappingCaptor.capture(), any() );

    List<ResourceMapping> capturedResourceMappings = resourceMappingCaptor.getAllValues();

    assertResourceMappingExists( capturedResourceMappings, "/pentaho-webpackage-1a", "/package-name-1a/1.0" );
    assertResourceMappingExists( capturedResourceMappings, "/pentaho-webpackage-1b", "/package-name-1b/1.1" );
    assertResourceMappingExists( capturedResourceMappings, "/pentaho-webpackage-1c", "/package-name-1c/1.2" );
  }

  @Test
  public void addWebPackageBundleWithResourcesOnRoot() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1a", "1.0", Bundle.ACTIVE );

    ArgumentCaptor<ResourceMapping> resourceMappingCaptor = ArgumentCaptor.forClass( ResourceMapping.class );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 1 ) ).registerService( eq( ResourceMapping.class.getName() ), resourceMappingCaptor.capture(), any() );

    List<ResourceMapping> capturedResourceMappings = resourceMappingCaptor.getAllValues();

    assertResourceMappingExists( capturedResourceMappings, "/", "/package-on-root/1.0" );
  }

  @Test
  public void addSameWebPackageBundle() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1b" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1c" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    ArgumentCaptor<ResourceMapping> resourceMappingCaptor = ArgumentCaptor.forClass( ResourceMapping.class );

    this.service.addBundle( bundle );

    reset( bundle.getBundleContext() );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 0 ) ).registerService( eq( ResourceMapping.class.getName() ), resourceMappingCaptor.capture(), any() );
  }

  @Test
  public void addWebPackageBundleWithMissingPackageJson() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/missing-package-json" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    ArgumentCaptor<ResourceMapping> resourceMappingCaptor = ArgumentCaptor.forClass( ResourceMapping.class );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 1 ) ).registerService( eq( ResourceMapping.class.getName() ), resourceMappingCaptor.capture(), any() );

    List<ResourceMapping> capturedResourceMappings = resourceMappingCaptor.getAllValues();

    assertResourceMappingExists( capturedResourceMappings, "/pentaho-webpackage-1a", "/package-name-1a/1.0" );
  }

  @Test
  public void addWebPackageBundleWithInvalidPackageJson() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/invalid" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    ArgumentCaptor<ResourceMapping> resourceMappingCaptor = ArgumentCaptor.forClass( ResourceMapping.class );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 1 ) ).registerService( eq( ResourceMapping.class.getName() ), resourceMappingCaptor.capture(), any() );

    List<ResourceMapping> capturedResourceMappings = resourceMappingCaptor.getAllValues();

    assertResourceMappingExists( capturedResourceMappings, "/pentaho-webpackage-1a", "/package-name-1a/1.0" );
  }

  @Test
  public void addNonWebPackageBundle() {
    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.ACTIVE );

    this.service.addBundle( bundle );

    verify( bundle.getBundleContext(), times( 0 ) ).registerService( eq( ResourceMapping.class.getName() ), any( ResourceMapping.class ), any() );
  }

  @Test
  public void addNoBundle() {
    this.service.addBundle( null );

    // nothing to test, it just shouldn't fail
  }

  @Test
  public void removeWebPackageBundle() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1b" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1c" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    this.service.addBundle( bundle );

    this.service.removeBundle( bundle );

    verify( this.serviceRegistrationMap.get( "/pentaho-webpackage-1a" ), times( 1 ) ).unregister();
    verify( this.serviceRegistrationMap.get( "/pentaho-webpackage-1b" ), times( 1 ) ).unregister();
    verify( this.serviceRegistrationMap.get( "/pentaho-webpackage-1c" ), times( 1 ) ).unregister();
  }

  @Test
  public void removeUnknownWebPackageBundle() {
    List<BundleCapability> capabilities = new ArrayList<>();
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1a" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1b" ) );
    capabilities.add( createMockWebPackageCapability( "/pentaho-webpackage-1c" ) );
    Bundle bundle = this.createMockWebPackageBundle( capabilities, "pentaho-webpackage-1", "1.0", Bundle.ACTIVE );

    this.service.removeBundle( bundle );

    // nothing to test, it just shouldn't fail
  }

  @Test
  public void removeNonWebPackageBundle() {
    Bundle bundle = this.createMockBundle( "mock-1", "1.0", Bundle.ACTIVE );

    this.service.addBundle( bundle );

    this.service.removeBundle( bundle );

    // nothing to test, it just shouldn't fail
  }

  @Test
  public void removeNoBundle() {
    this.service.removeBundle( null );

    // nothing to test, it just shouldn't fail
  }

  public void assertResourceMappingExists( List<ResourceMapping> capturedResourceMappings, String expectedRoot, String expectedAlias ) {
    for ( ResourceMapping resourceMapping : capturedResourceMappings ) {
      if ( resourceMapping.getPath().equals( expectedRoot ) && resourceMapping.getAlias().equals( expectedAlias ) ) {
        return;
      }
    }

    fail( "ResourceMapping(" + expectedRoot + ", " + expectedAlias + ") not found." );
  }

  private Bundle createMockBundle( String bundleName, String bundleVersion, int bundleState ) {
    Bundle mockBundle = mock( Bundle.class );

    when( mockBundle.getBundleId() ).thenReturn( ++this.mockBundleCounter );

    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );

    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );

    when( mockBundle.getState() ).thenReturn( bundleState );

    Dictionary<String, String> headers = new Hashtable<>();
    when( mockBundle.getHeaders() ).thenReturn( headers );

    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );

    when( mockBundleContext
        .registerService( eq( ResourceMapping.class.getName() ), any( ResourceMapping.class ), any() ) )
        .thenAnswer( (Answer<ServiceRegistration<?>>) invocation -> {
          final ServiceRegistration mockServiceRegistration = mock( ServiceRegistration.class );

          PentahoWebPackageServiceImplTest.this.serviceRegistrationMap.put( ( (ResourceMapping) invocation.getArguments()[1] ).getPath(), mockServiceRegistration );

          return mockServiceRegistration;
        } );

    return mockBundle;
  }

  private Bundle createMockWebPackageBundle( List<BundleCapability> capabilities, String bundleName, String bundleVersion, int bundleState ) {
    final Bundle mockBundle = this.createMockBundle( bundleName, bundleVersion, bundleState );

    BundleWiring wiring = mock( BundleWiring.class );

    Dictionary<String, String> headers = mockBundle.getHeaders();
    List<BundleCapability> bundleCapabilities = new ArrayList<>();

    List<String> capabilitiesHeader = new ArrayList<>();
    capabilities.forEach( bundleCapability -> {
      bundleCapabilities.add( bundleCapability );

      String root = bundleCapability.getAttributes().get( "root" ).toString();
      while ( root.endsWith( "/" ) ) {
        root = root.substring( 0, root.length() - 1 );
      }

      capabilitiesHeader.add( PentahoWebPackageService.CAPABILITY_NAMESPACE + ";root=\"" + root + "\"" );

      when( mockBundle.getResource( root + "/package.json" ) ).thenReturn( this.getClass().getClassLoader().getResource( "./" + ( root.isEmpty() ? "root" : root ) + "-package.json" ) );
    } );

    when( wiring.getCapabilities( PentahoWebPackageService.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilities );

    headers.put( "Provide-Capability", String.join( ",", capabilitiesHeader ) );

    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( wiring );

    return mockBundle;
  }

  private BundleCapability createMockWebPackageCapability( String root ) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", root );

    BundleCapability bundleCapability = mock( BundleCapability.class );
    when( bundleCapability.getAttributes() ).thenReturn( attributes );

    return bundleCapability;
  }

  private BundleEvent createMockBundleEvent( Bundle bundle, int status ) {
    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    when( mockBundleEvent.getBundle() ).thenReturn( bundle );
    when( mockBundleEvent.getType() ).thenReturn( status );

    return mockBundleEvent;
  }
}
