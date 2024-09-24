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
package org.pentaho.osgi.impl;

import org.apache.karaf.bundle.core.BundleState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueprintStateServiceImplTest {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  BlueprintStateServiceImpl blueprintStateServiceImpl;
  List<Bundle> bundleList;

  private static final long MOCK_BUNDLE_UNKNOWN_ID = 1;
  private static final long MOCK_BUNDLE_STARTING_ID = 2;
  private static final long MOCK_BUNDLE_ACTIVE_ID = 3;
  private static final long MOCK_BUNDLE_STOPPING_ID = 4;
  private static final long MOCK_BUNDLE_RESOLVED_ID = 5;
  private static final long MOCK_BUNDLE_FAILURE_ID = 6;
  private static final long MOCK_BUNDLE_GRACE_PERIOD_ID = 7;
  private static final long MOCK_BUNDLE_WAITING_ID = 8;
  private static final long MOCK_BUNDLE_NO_BLUEPRINT_ID = 9;

  private static final String[] MOCK_BUNDLE_NO_DEPENDENCIES = null;
  private static final String[] MOCK_BUNDLE_FAILED_DEPENDENCIES = { "dependency1", "dependency2" };
  private static final String[] MOCK_BUNDLE_WAITING_DEPENDENCIES = { "dependency3", "dependency4", "dependency5" };
  private static final String[] MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES = { "dependency6" };

  private static final Throwable MOCK_BUNDLE_NO_CAUSE = null;
  private static final Throwable MOCK_BUNDLE_FAILED_CAUSE = new Throwable();

  @Before
  public void setUp() throws NoSuchMethodException {
    BundleContext bundleContext = mock( BundleContext.class );
    bundleList = new ArrayList<Bundle>();

    Bundle unknownBundle = createMockBundle( MOCK_BUNDLE_UNKNOWN_ID, true );
    bundleList.add( unknownBundle );

    Bundle startingBundle = createMockBundle( MOCK_BUNDLE_STARTING_ID, true );
    BlueprintEvent startingEvent =
        createMockBlueprintEvent( startingBundle, BlueprintEvent.CREATING, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( startingBundle );

    Bundle activeBundle = createMockBundle( MOCK_BUNDLE_ACTIVE_ID, true );
    BlueprintEvent activeEvent =
        createMockBlueprintEvent( activeBundle, BlueprintEvent.CREATED, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( activeBundle );

    Bundle stoppingBundle = createMockBundle( MOCK_BUNDLE_STOPPING_ID, true );
    BlueprintEvent stoppingEvent =
        createMockBlueprintEvent( stoppingBundle, BlueprintEvent.DESTROYING, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( stoppingBundle );

    Bundle resolvedBundle = createMockBundle( MOCK_BUNDLE_RESOLVED_ID, true );
    BlueprintEvent resolvedEvent =
        createMockBlueprintEvent( resolvedBundle, BlueprintEvent.DESTROYED, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( resolvedBundle );

    Bundle failBundle = createMockBundle( MOCK_BUNDLE_FAILURE_ID, true );
    BlueprintEvent failEvent =
        createMockBlueprintEvent( failBundle, BlueprintEvent.FAILURE, MOCK_BUNDLE_FAILED_DEPENDENCIES,
            MOCK_BUNDLE_FAILED_CAUSE );
    bundleList.add( failBundle );

    Bundle gracePeriodBundle = createMockBundle( MOCK_BUNDLE_GRACE_PERIOD_ID, true );
    BlueprintEvent gracePeriodEvent =
        createMockBlueprintEvent( gracePeriodBundle, BlueprintEvent.GRACE_PERIOD, MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( gracePeriodBundle );

    Bundle waitingBundle = createMockBundle( MOCK_BUNDLE_WAITING_ID, true );
    BlueprintEvent waitingEvent =
        createMockBlueprintEvent( waitingBundle, BlueprintEvent.WAITING, MOCK_BUNDLE_WAITING_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE );
    bundleList.add( waitingBundle );

    Bundle noBlueprintBundle = createMockBundle( MOCK_BUNDLE_NO_BLUEPRINT_ID, false );
    bundleList.add( noBlueprintBundle );

    blueprintStateServiceImpl = new BlueprintStateServiceImpl( bundleContext );
    Bundle[] bundles = bundleList.toArray( new Bundle[0] );
    when( bundleContext.getBundles() ).thenReturn( bundles );

    blueprintStateServiceImpl.blueprintEvent( startingEvent );
    blueprintStateServiceImpl.blueprintEvent( activeEvent );
    blueprintStateServiceImpl.blueprintEvent( stoppingEvent );
    blueprintStateServiceImpl.blueprintEvent( resolvedEvent );
    blueprintStateServiceImpl.blueprintEvent( failEvent );
    blueprintStateServiceImpl.blueprintEvent( gracePeriodEvent );
    blueprintStateServiceImpl.blueprintEvent( waitingEvent );
  }

  @Test
  public void testIsBlueprintLoaded() {
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_UNKNOWN_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_STARTING_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_ACTIVE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_STOPPING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_RESOLVED_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_FAILURE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_WAITING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_NO_BLUEPRINT_ID ) );

    bundleStartingToActive();
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintLoaded( MOCK_BUNDLE_STARTING_ID ) );
  }

  @Test
  public void testIsBlueprintFailed() {
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_UNKNOWN_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_STARTING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_ACTIVE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_STOPPING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_RESOLVED_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_FAILURE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_WAITING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_NO_BLUEPRINT_ID ) );

    bundleGracePeriodToFailed();
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
  }


  @Test
  public void testIsBlueprintTrying() {
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_STARTING_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_WAITING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_UNKNOWN_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_ACTIVE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_STOPPING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_RESOLVED_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_FAILURE_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.isBlueprintTryingToLoad( MOCK_BUNDLE_NO_BLUEPRINT_ID ) );

    bundleGracePeriodToFailed();
    Assert.assertTrue( blueprintStateServiceImpl.isBlueprintFailed( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
  }


  @Test
  public void testHasBlueprint() {
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_UNKNOWN_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_STARTING_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_ACTIVE_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_STOPPING_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_RESOLVED_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_FAILURE_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_GRACE_PERIOD_ID ) );
    Assert.assertTrue( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_WAITING_ID ) );
    Assert.assertFalse( blueprintStateServiceImpl.hasBlueprint( MOCK_BUNDLE_NO_BLUEPRINT_ID ) );
  }

  @Test
  public void testGetBundleState() {
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_UNKNOWN_ID ).equals(
        BundleState.Unknown ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_STARTING_ID ).equals(
        BundleState.Starting ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_ACTIVE_ID ).equals( BundleState.Active ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_STOPPING_ID ).equals(
        BundleState.Stopping ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_RESOLVED_ID ).equals(
        BundleState.Resolved ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_FAILURE_ID ).equals(
        BundleState.Failure ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_GRACE_PERIOD_ID ).equals(
        BundleState.GracePeriod ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_WAITING_ID ).equals(
        BundleState.Waiting ) );
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_NO_BLUEPRINT_ID ).equals(
        BundleState.Unknown ) );

    bundleGracePeriodToFailed();
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_GRACE_PERIOD_ID ).equals(
        BundleState.Failure ) );

    bundleStartingToActive();
    Assert.assertTrue( blueprintStateServiceImpl.getBundleState( MOCK_BUNDLE_STARTING_ID ).equals(
        BundleState.Active ) );
  }

  @Test
  public void testGetBundleMissDependencies() {
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_UNKNOWN_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_STARTING_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_ACTIVE_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_STOPPING_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_RESOLVED_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_FAILURE_ID ),
        MOCK_BUNDLE_FAILED_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_GRACE_PERIOD_ID ),
        MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_WAITING_ID ),
        MOCK_BUNDLE_WAITING_DEPENDENCIES );
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_NO_BLUEPRINT_ID ),
        MOCK_BUNDLE_NO_DEPENDENCIES );

    bundleGracePeriodToFailed();
    assertDependencies( blueprintStateServiceImpl.getBundleMissDependencies( MOCK_BUNDLE_GRACE_PERIOD_ID ),
        MOCK_BUNDLE_FAILED_DEPENDENCIES );

  }

  @Test
  public void testBundleFailureCause() {
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_UNKNOWN_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_STARTING_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_ACTIVE_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_STOPPING_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_RESOLVED_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_FAILURE_ID ),
        MOCK_BUNDLE_FAILED_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_GRACE_PERIOD_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_WAITING_ID ),
        MOCK_BUNDLE_NO_CAUSE );
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_NO_BLUEPRINT_ID ),
        MOCK_BUNDLE_NO_CAUSE );

    bundleGracePeriodToFailed();
    Assert.assertEquals( blueprintStateServiceImpl.getBundleFailureCause( MOCK_BUNDLE_GRACE_PERIOD_ID ),
        MOCK_BUNDLE_FAILED_CAUSE );

  }

  @After
  public void tearDown() {
    blueprintStateServiceImpl = null;
    bundleList = null;
  }

  private void bundleGracePeriodToFailed() {
    BlueprintEvent failEvent =
        createMockBlueprintEvent( getBundle( MOCK_BUNDLE_GRACE_PERIOD_ID ), BlueprintEvent.FAILURE,
            MOCK_BUNDLE_FAILED_DEPENDENCIES, MOCK_BUNDLE_FAILED_CAUSE );

    blueprintStateServiceImpl.blueprintEvent( failEvent );
  }

  private void bundleStartingToActive() {
    BlueprintEvent activeEvent =
        createMockBlueprintEvent( getBundle( MOCK_BUNDLE_STARTING_ID ), BlueprintEvent.CREATED,
            MOCK_BUNDLE_NO_DEPENDENCIES, MOCK_BUNDLE_NO_CAUSE );

    blueprintStateServiceImpl.blueprintEvent( activeEvent );
  }

  private Bundle getBundle( long bundleId ) {
    for ( Bundle bundle : bundleList ) {
      if ( bundle.getBundleId() == bundleId ) {
        return bundle;
      }
    }

    return null;
  }

  private void assertDependencies( String[] s1, String[] s2 ) {
    if ( s1 == null || s2 == null ) {
      if ( s1 == s2 ) {
        return;
      }
      Assert.fail();
      return;
    }

    String[] s1Copy = Arrays.copyOf( s1, s1.length );
    Arrays.sort( s1Copy );

    String[] s2Copy = Arrays.copyOf( s2, s2.length );
    Arrays.sort( s2Copy );

    Assert.assertTrue( Arrays.deepEquals( s1Copy, s2Copy ) );
  }

  private Bundle createMockBundle( long bundleId, boolean hasBlueprint ) {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getBundleId() ).thenReturn( bundleId );

    if ( hasBlueprint ) {
      try {
        when( bundle.getResource( "OSGI-INF/blueprint" ) ).thenReturn( new URL( "http://test.com/" ) );
      } catch ( MalformedURLException e ) {
        logger.error( e.getMessage(), e );
      }
    } else {
      when( bundle.getResource( "OSGI-INF/blueprint" ) ).thenReturn( null );
    }
    return bundle;
  }

  private BlueprintEvent createMockBlueprintEvent( Bundle bundle, int type, String[] bundleMissDependencies,
      Throwable bundleFailureCause ) {
    BlueprintEvent blueprintEvent = mock( BlueprintEvent.class );
    when( blueprintEvent.getBundle() ).thenReturn( bundle );
    when( blueprintEvent.getType() ).thenReturn( type );
    when( blueprintEvent.getDependencies() ).thenReturn( bundleMissDependencies );
    when( blueprintEvent.getCause() ).thenReturn( bundleFailureCause );
    return blueprintEvent;
  }
}
