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
package org.pentaho.webpackage.extender.requirejs.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PentahoWebPackageServiceTrackerTest {

  private PentahoWebPackageServiceTracker pentahoWebPackageServiceTracker;

  @Before
  public void setUp() {
  }

  @Test
  public void testAddingServiceWithValidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();
    doReturn( mockBundleContext ).when( mockBundle ).getBundleContext();
    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );


    // act
    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNotNull( "Should return valid IRequireJsPackage object", requireJsPackage );
  }

  @Test
  public void testAddingServiceWithInvalidServiceReference() throws Exception {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( null ).when( mockServiceReference ).getBundle();
    doReturn( mockBundleContext ).when( mockBundle ).getBundleContext();
    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );

    // act
    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    // assert
    assertNull( "Should return null", requireJsPackage );
  }

  @Test
  public void removedService() throws Exception {
    // arrange
    Bundle mockBundle = mock( Bundle.class );
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceReference mockServiceReference = mock( ServiceReference.class );
    doReturn( mockBundle ).when( mockServiceReference ).getBundle();
    doReturn( mockBundleContext ).when( mockBundle ).getBundleContext();
    IPentahoWebPackage pentahoWebPackage = mock( IPentahoWebPackage.class );
    doReturn( pentahoWebPackage ).when( mockBundleContext ).getService( any() );

    this.pentahoWebPackageServiceTracker = new PentahoWebPackageServiceTracker( mockBundleContext );

    IRequireJsPackage requireJsPackage =
        this.pentahoWebPackageServiceTracker.addingService( mockServiceReference );

    IRequireJsPackage mockRequireJsPackage = spy( requireJsPackage );

    // act
    this.pentahoWebPackageServiceTracker.removedService( mockServiceReference, mockRequireJsPackage );

    // assert
    verify( mockBundleContext, times( 1 ) ).ungetService( any() );
    verify( mockRequireJsPackage, times( 1 ) ).unregister();
  }
}
