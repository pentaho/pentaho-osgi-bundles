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
package org.pentaho.webpackage.extender.http.impl.osgi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.net.URL;
import java.util.Dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PentahoWebPackageServiceTrackerTest {
  private Bundle mockBundle;
  private BundleContext mockBundleContext;
  private ServiceReference mockServiceReference;
  private ServiceRegistration mockServiceRegistration;

  private IPentahoWebPackage mockPentahoWebPackage;
  private String mockWebRootPath;
  private String mockResourceRootPath;

  private URL resourceRootUrl;

  private PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker;

  private static final String RESOURCE_PATTERN_KEY = "osgi.http.whiteboard.resource.pattern";
  private static final String RESOURCE_PREFIX_KEY = "osgi.http.whiteboard.resource.prefix";

  @Before
  public void setUp() {
    this.mockBundle = mock( Bundle.class );

    this.mockBundleContext = mock( BundleContext.class );

    this.mockServiceReference = mock( ServiceReference.class );
    doReturn( this.mockBundle ).when( mockServiceReference ).getBundle();
    doReturn( this.mockBundleContext ).when( this.mockBundle ).getBundleContext();

    this.mockServiceRegistration = mock( ServiceRegistration.class );
    doReturn( this.mockServiceRegistration ).when( this.mockBundleContext ).registerService( anyString(), any(), any( Dictionary.class ) );

    this.mockPentahoWebPackage = mock( IPentahoWebPackage.class );
    this.mockWebRootPath = "/web/path";
    this.mockResourceRootPath = "/resource/path";
    doReturn( this.mockWebRootPath ).when( this.mockPentahoWebPackage ).getWebRootPath();
    doReturn( this.mockResourceRootPath ).when( this.mockPentahoWebPackage ).getResourceRootPath();

    doReturn( this.mockPentahoWebPackage ).when( this.mockBundleContext ).getService( any() );

    this.resourceRootUrl = this.getClass().getResource( "/" );
    doReturn( this.resourceRootUrl ).when( this.mockBundle ).getResource( anyString() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( this.mockBundleContext );
  }


  @Test
  public void testAddingServiceWithValidServiceReference() throws Exception {
    // act
    ServiceRegistration serviceRegistration = pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNotNull( serviceRegistration );

    ArgumentCaptor<Dictionary> servicePropertiesCaptor = ArgumentCaptor.forClass(Dictionary.class);

    verify( this.mockBundleContext, times( 1 ) ).registerService( eq( String.class.getName() ), eq( "" ), servicePropertiesCaptor.capture() );

    Dictionary serviceProperties = servicePropertiesCaptor.getValue();

    assertEquals( this.mockWebRootPath + "/*", serviceProperties.get( RESOURCE_PATTERN_KEY ) );
    assertEquals( this.mockResourceRootPath, serviceProperties.get( RESOURCE_PREFIX_KEY ) );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() throws Exception {
    // arrange
    doReturn( null ).when( this.mockServiceReference ).getBundle();

    // act
    ServiceRegistration serviceRegistration = pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( serviceRegistration );

    verify( mockBundleContext, times( 0 ) ).registerService( anyString(), any(), any() );
  }

  @Test
  public void testRemovedServiceShouldCallBundleContextUngetServiceAndPentahoWebPackageResourceMappingUnregisterOnce() throws Exception {
    // arrange
    ServiceRegistration serviceRegistration = this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // act
    this.pentahoWebPackageServiceTracker.removedService( this.mockServiceReference, serviceRegistration );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( any() );
    verify( serviceRegistration, times( 1 ) ).unregister();
  }
}