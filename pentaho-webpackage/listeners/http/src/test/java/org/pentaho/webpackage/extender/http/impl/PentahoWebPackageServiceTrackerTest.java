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
package org.pentaho.webpackage.extender.http.impl;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.webpackage.core.IPentahoWebPackage;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebPackageServiceTrackerTest {


  @Test
  public void testAddingServiceWithValidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping =
        pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNotNull( pentahoWebPackageResourceMapping );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = null;
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();
    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping =
        pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( pentahoWebPackageResourceMapping );
  }

  // This test is just for coverage
  @Test
  public void modifiedService() throws Exception {
    // arrange
    BundleContext mockBundleContext = mock( BundleContext.class );
    PentahoWebPackageResourceMapping mockPentahoWebPackageResourceMapping = mock( PentahoWebPackageResourceMapping.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    pentahoWebPackageServiceTracker.modifiedService( mockServiceReference, mockPentahoWebPackageResourceMapping );
  }

  @Test
  public void testRemovedServiceShouldCallBundleContextUngetServiceAndPentahoWebPackageResourceMappingUnregisterOnce() throws Exception {
    // arrange
    Bundle mockBundle = this.createBaseMockBundle();
    BundleContext mockBundleContext = mockBundle.getBundleContext();
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();

    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class ); // new PentahoWebPackageImpl( null, mockUrl );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker =
        new PentahoWebPackageServiceTracker( mockBundleContext );

    PentahoWebPackageResourceMapping mockPentahoWebPackageResourceMapping = mock( PentahoWebPackageResourceMapping.class );

    // act
    pentahoWebPackageServiceTracker.removedService( mockServiceReference, mockPentahoWebPackageResourceMapping );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( mockServiceReference );
    verify( mockPentahoWebPackageResourceMapping, times( 1 ) ).unregister();
  }

  private Bundle createBaseMockBundle() {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( "SomeBundleName" );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( "1.1.1.1" );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( Bundle.ACTIVE );

    ServiceRegistration mockServiceReference = mock( ServiceRegistration.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );
    when( mockBundleContext.registerService( eq( IPentahoWebPackage.class.getName() ), any(), any() ) )
        .thenReturn( mockServiceReference );

    return mockBundle;

  }

}