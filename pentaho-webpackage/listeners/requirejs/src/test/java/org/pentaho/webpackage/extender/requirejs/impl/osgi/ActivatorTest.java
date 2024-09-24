/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.webpackage.extender.requirejs.impl.osgi;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ActivatorTest {
  private Activator activator;

  @Before
  public void setUp() {
    this.activator = new Activator();
  }

  @Test
  public void start() {
    // arrange
    BundleContext mockBundleContext = mock( BundleContext.class );
    ServiceTracker serviceTrackerMock = spy( activator.createPentahoWebPackageServiceTracker( mockBundleContext ) );
    activator = spy( new Activator() );
    doReturn( serviceTrackerMock ).when( activator ).createPentahoWebPackageServiceTracker( mockBundleContext );

    // act
    activator.start( mockBundleContext );

    // assert
    verify( serviceTrackerMock, times( 1 ) ).open( true );
  }

  @Test
  public void serviceTrackerIsClosedOnActivatorStop() {
    // arrange
    ServiceTracker mockServiceTracker = mock( ServiceTracker.class );
    activator.pentahoWebPackageServiceTracker = mockServiceTracker;

    // act
    activator.stop( null /* value doesnt matter */ );

    // assert
    verify( mockServiceTracker, times( 1 ) ).close();
  }

  @Test
  public void serviceTrackerIsSetToNullOnActivatorStop() {
    // arrange
    activator.pentahoWebPackageServiceTracker = mock( ServiceTracker.class );

    // act
    activator.stop( null /* value doesnt matter */ );

    // assert
    assertNull( activator.pentahoWebPackageServiceTracker );
  }
}
