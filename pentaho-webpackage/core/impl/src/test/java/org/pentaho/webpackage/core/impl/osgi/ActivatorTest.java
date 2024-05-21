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

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ActivatorTest {

  @Test
  public void bundleListenerIsAddedToBundleContextOnActivatorStart() {
    // arrange
    PentahoWebPackageBundleListener bundleListener = mock( PentahoWebPackageBundleListener.class );
    Activator activator = createActivatorSpy( bundleListener );
    BundleContext bundleContext = mock( BundleContext.class );

    // act
    activator.start( bundleContext );

    // assert
    verify( bundleContext ).addBundleListener( bundleListener );
  }

  @Test
  public void listenerRegisterIsCalledForEveryActiveBundleOnActivatorStart() {
    // arrange
    PentahoWebPackageBundleListener bundleListener = mock( PentahoWebPackageBundleListener.class );
    Activator activator = createActivatorSpy( bundleListener );

    Collection<Bundle> activeBundles = new ArrayList<>();
    int numberOfActiveBundles = 3;
    for ( int iBundle = 0; iBundle < numberOfActiveBundles; iBundle++ ) {
      activeBundles.add( createMockBundle( Bundle.ACTIVE ) );
    }

    BundleContext bundleContext = mock( BundleContext.class );
    doReturn( activeBundles.toArray( new Bundle[0] ) ).when( bundleContext ).getBundles();

    // act
    activator.start( bundleContext );

    // assert
    verify( bundleListener, times( numberOfActiveBundles ) ).registerWebPackageServices( any() );
  }


  @Test
  public void listenerRegisterIsNotCalledForBundlesNotActiveOnActivatorStart() {
    // arrange
    PentahoWebPackageBundleListener bundleListener = mock( PentahoWebPackageBundleListener.class );
    Activator activator = createActivatorSpy( bundleListener );

    Collection<Bundle> nonActiveBundles = new ArrayList<>();
    nonActiveBundles.add( createMockBundle( Bundle.UNINSTALLED ) );
    nonActiveBundles.add( createMockBundle( Bundle.INSTALLED ) );
    nonActiveBundles.add( createMockBundle( Bundle.RESOLVED ) );
    nonActiveBundles.add( createMockBundle( Bundle.STARTING ) );
    nonActiveBundles.add( createMockBundle( Bundle.STOPPING ) );

    BundleContext bundleContext = mock( BundleContext.class );
    doReturn( nonActiveBundles.toArray( new Bundle[0] ) ).when( bundleContext ).getBundles();

    // act
    activator.start( bundleContext );

    // assert
    verify( bundleListener, never() ).registerWebPackageServices( any() );
  }

  @Test
  public void bundleListenerIsRemovedFromBundleContextOnActivatorStop() {
    // arrange
    PentahoWebPackageBundleListener bundleListener = mock( PentahoWebPackageBundleListener.class );
    Activator activator = createActivatorSpy( bundleListener );
    BundleContext bundleContext = mock( BundleContext.class );
    // starting the activator to set the private activator.pentahoWebPackageBundleListener to bundleListener
    activator.start( bundleContext );

    // act
    activator.stop( bundleContext );

    // assert
    verify( bundleContext ).removeBundleListener( bundleListener );
  }

  private Bundle createMockBundle( int bundleState ) {
    Bundle bundle = mock( Bundle.class );
    doReturn( bundleState ).when( bundle ).getState();
    return bundle;
  }

  private Activator createActivatorSpy( PentahoWebPackageBundleListener bundleListener ) {
    Activator activator = spy( new Activator() );
    doReturn( bundleListener ).when( activator ).createPentahoWebPackageService();
    return activator;
  }

  @Test
  public void testCreatePentahoWebPackageService() {
    // arange
    PentahoWebPackageBundleListener pentahoWebPackageBundleListener;
    Activator activator = spy( new Activator() );

    // act
    pentahoWebPackageBundleListener = activator.createPentahoWebPackageService();

    // assert
    assertNotNull( pentahoWebPackageBundleListener );
  }
}
