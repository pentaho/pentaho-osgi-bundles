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
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.osgi.api.BlueprintStateService;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.osgi.api.IKarafBlueprintWatcher.BlueprintWatcherException;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class KarafBlueprintWatcherImplTest {
  private static final long MOCK_BUNDLE_ACTIVE_ID = 3;
  private static final long MOCK_BUNDLE_FAILURE_ID = 6;
  private static final long MOCK_BUNDLE_GRACE_PERIOD_ID = 7;
  private static final long MOCK_BUNDLE_NO_BLUEPRINT_ID = 9;
  private static final long MOCK_BUNDLE_UNKNOWN_ID = 12;
  private static final long MOCK_BUNDLE_INSTALLED_ID = 15;

  private static final String[] MOCK_BUNDLE_NO_DEPENDENCIES = null;
  private static final String[] MOCK_BUNDLE_FAILED_DEPENDENCIES = { "dependency1", "dependency2" };
  private static final String[] MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES = { "dependency6" };

  private static final Throwable MOCK_BUNDLE_NO_CAUSE = null;
  private static final StackTraceElement[] STACK_TRACE = {
    new StackTraceElement( "declaringClass1", "methodName1", "filename1", 1 ),
    new StackTraceElement( "declaringClass2", "methodName2", "filename2", 2 ) };
  private static final Throwable MOCK_BUNDLE_FAILED_CAUSE = new Throwable();

  private static final String EXPECTED_REPORT_HEADER =
      System.lineSeparator() + "--------- Karaf Blueprint Watcher Report Begin ---------";
  private static final String EXPECTED_REPORT_FOR_FAILED_BLUEPRINT =
      "\n--------- Karaf Blueprint Watcher Report Begin ---------\nBlueprint Bundle(s) not loaded:\n\tBlueprint Bundle 'Bundle_6':\n \t\t Blueprint Bundle State: Failure\n \t\t Blueprint Bundle ID: 6\n \t\t Missing Dependencies:\n \t\t \tdependency1\n \t\t \tdependency2\n \t\t This blueprint state was caused by: \n \t\t \tjava.lang.Throwable\n \t \t \t\tat declaringClass1.methodName1(filename1:1)\n \t \t \t\tat declaringClass2.methodName2(filename2:2)\n--------- Karaf Blueprint Watcher Report End ---------";
  private static final String EXPECTED_REPORT_FOR_UNLOADED_BLUEPRINTS =
      "\n--------- Karaf Blueprint Watcher Report Begin ---------\nBlueprint Bundle(s) not loaded:\n\tBlueprint Bundle 'Bundle_6':\n \t\t Blueprint Bundle State: Failure\n \t\t Blueprint Bundle ID: 6\n \t\t Missing Dependencies:\n \t\t \tdependency1\n \t\t \tdependency2\n \t\t This blueprint state was caused by: \n \t\t \tjava.lang.Throwable\n \t \t \t\tat declaringClass1.methodName1(filename1:1)\n \t \t \t\tat declaringClass2.methodName2(filename2:2)\n\tBlueprint Bundle 'Bundle_12':\n \t\t Blueprint Bundle State: Unknown\n \t\t Blueprint Bundle ID: 12\n\tBlueprint Bundle 'Bundle_7':\n \t\t Blueprint Bundle State: GracePeriod\n \t\t Blueprint Bundle ID: 7\n \t\t Missing Dependencies:\n \t\t \tdependency6\n--------- Karaf Blueprint Watcher Report End ---------";

  public List<String> messages = new ArrayList<String>();

  private class TestAppender extends AppenderSkeleton {

    public TestAppender() {
      super();
      messages = new ArrayList<String>();
    }

    public void append( LoggingEvent event ) {
      if ( event.getLevel().equals( Level.DEBUG ) ) {
        messages.add( event.getMessage().toString() );
      }
    }

    @Override
    public void close() {
      messages = null;
    }

    @Override
    public boolean requiresLayout() {
      return false;
    }

  }

  @Before
  public void setUp() {
    LogManager.getRootLogger().addAppender( new TestAppender() );
    MOCK_BUNDLE_FAILED_CAUSE.setStackTrace( STACK_TRACE );

  }


  @Test
  public void testNonResolvedBundlesSkipped() {

    // Setting the karaf timeout to 1ms, because this is an unit test
    IApplicationContext applicationContext = mock( IApplicationContext.class );
    PentahoSystem.init( applicationContext );
    when( applicationContext.getProperty( KarafBlueprintWatcherImpl.KARAF_TIMEOUT_PROPERTY ) ).thenReturn( "1" );


    BundleContext bundleContext = mock( BundleContext.class );

    @SuppressWarnings( "unchecked" )
    ServiceReference<BlueprintStateService> blueprintStateServiceReference =
        (ServiceReference<BlueprintStateService>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( BlueprintStateService.class ) ).thenReturn(
        blueprintStateServiceReference );

    BlueprintStateService blueprintStateService = mock( BlueprintStateService.class );
    when( bundleContext.getService( blueprintStateServiceReference ) ).thenReturn( blueprintStateService );

    KarafBlueprintWatcherImpl karafBlueprintWatcherImpl = new KarafBlueprintWatcherImpl( bundleContext );

    // Test when all blueprints are loaded

    List<Bundle> bundleList = new ArrayList<Bundle>();

    Bundle activeBundle =
        createMockBundle( MOCK_BUNDLE_ACTIVE_ID, true, Bundle.RESOLVED, BundleState.Active, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE, blueprintStateService );

    Bundle installedBundle =
        createMockBundle( MOCK_BUNDLE_INSTALLED_ID, true, Bundle.STARTING, BundleState.Installed, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE, blueprintStateService );

    bundleList.add( activeBundle );
    bundleList.add( installedBundle );


    Bundle[] bundles = bundleList.toArray( new Bundle[bundleList.size()] );
    when( bundleContext.getBundles() ).thenReturn( bundles );

    try {
      karafBlueprintWatcherImpl.waitForBlueprint();
    } catch ( BlueprintWatcherException e ) {
      Assert.fail();
    }
  }

  @Test
  public void testWaitForBlueprint() {
    // Setting the karaf timeout to 1ms, because this is an unit test
    IApplicationContext applicationContext = mock( IApplicationContext.class );
    PentahoSystem.init( applicationContext );
    when( applicationContext.getProperty( KarafBlueprintWatcherImpl.KARAF_TIMEOUT_PROPERTY ) ).thenReturn( "1" );

    BundleContext bundleContext = mock( BundleContext.class );

    @SuppressWarnings( "unchecked" )
    ServiceReference<BlueprintStateService> blueprintStateServiceReference =
        (ServiceReference<BlueprintStateService>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( BlueprintStateService.class ) ).thenReturn(
        blueprintStateServiceReference );

    BlueprintStateService blueprintStateService = mock( BlueprintStateService.class );
    when( bundleContext.getService( blueprintStateServiceReference ) ).thenReturn( blueprintStateService );

    KarafBlueprintWatcherImpl karafBlueprintWatcherImpl = new KarafBlueprintWatcherImpl( bundleContext );

    // Test when all blueprints are loaded

    List<Bundle> bundleList = new ArrayList<Bundle>();

    Bundle activeBundle =
        createMockBundle( MOCK_BUNDLE_ACTIVE_ID, true, Bundle.RESOLVED, BundleState.Active, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE, blueprintStateService );
    bundleList.add( activeBundle );

    Bundle noBlueprintBundle =
        createMockBundle( MOCK_BUNDLE_NO_BLUEPRINT_ID, false, Bundle.RESOLVED, BundleState.Unknown, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE, blueprintStateService );
    bundleList.add( noBlueprintBundle );

    Bundle[] bundles = bundleList.toArray( new Bundle[bundleList.size()] );
    when( bundleContext.getBundles() ).thenReturn( bundles );

    try {
      karafBlueprintWatcherImpl.waitForBlueprint();
    } catch ( BlueprintWatcherException e ) {
      Assert.fail();
    }

    // Test when all blueprints are either loaded or failed

    Bundle failureBundle =
        createMockBundle( MOCK_BUNDLE_FAILURE_ID, true, Bundle.RESOLVED, BundleState.Failure, MOCK_BUNDLE_FAILED_DEPENDENCIES,
            MOCK_BUNDLE_FAILED_CAUSE, blueprintStateService );
    bundleList.add( failureBundle );
    bundles = bundleList.toArray( new Bundle[bundleList.size()] );
    when( bundleContext.getBundles() ).thenReturn( bundles );

    try {
      karafBlueprintWatcherImpl.waitForBlueprint();
    } catch ( BlueprintWatcherException e ) {
      Assert.fail();
    }

    // Test log debug output for failed blueprint
    String debugOutput = WatchersTestUtils.findKarafDebugOutput( messages, EXPECTED_REPORT_HEADER );
    if ( debugOutput == null ) {
      Assert.fail();
    }
    WatchersTestUtils.testEquivalentReports( debugOutput, EXPECTED_REPORT_FOR_FAILED_BLUEPRINT );

    // Test when blueprints are loaded, failed and unloaded

    Bundle unknownBundle =
        createMockBundle( MOCK_BUNDLE_UNKNOWN_ID, true, Bundle.RESOLVED, BundleState.Unknown, MOCK_BUNDLE_NO_DEPENDENCIES,
            MOCK_BUNDLE_NO_CAUSE, blueprintStateService );
    bundleList.add( unknownBundle );

    Bundle gracePeriodBundle =
        createMockBundle( MOCK_BUNDLE_GRACE_PERIOD_ID, true, Bundle.RESOLVED, BundleState.GracePeriod,
            MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES, MOCK_BUNDLE_NO_CAUSE, blueprintStateService );
    bundleList.add( gracePeriodBundle );

    bundles = bundleList.toArray( new Bundle[bundleList.size()] );
    when( bundleContext.getBundles() ).thenReturn( bundles );

    try {
      karafBlueprintWatcherImpl.waitForBlueprint();
    } catch ( BlueprintWatcherException e ) {
      Throwable cause = e.getCause();
      Assert.assertTrue( cause instanceof IKarafBlueprintWatcher.BlueprintWatcherException );

      String message = e.getCause().getMessage();
      Assert.assertFalse( message.contains( WatchersTestUtils.getBundleName( MOCK_BUNDLE_UNKNOWN_ID ) ) );
      Assert.assertTrue( message.contains( WatchersTestUtils.getBundleName( MOCK_BUNDLE_GRACE_PERIOD_ID ) ) );

      String debugOutputUnloadedBlueprints = WatchersTestUtils.findKarafDebugOutput( messages, EXPECTED_REPORT_HEADER );
      if ( debugOutputUnloadedBlueprints == null ) {
        Assert.fail();
      }
      WatchersTestUtils.testEquivalentReports( debugOutputUnloadedBlueprints, EXPECTED_REPORT_FOR_UNLOADED_BLUEPRINTS );
      return;
    }
    Assert.fail();
  }

  private Bundle createMockBundle( long bundleId, boolean hasBlueprint, int bundleState, BundleState blueprintState, String[] dependencies,
      Throwable cause, BlueprintStateService blueprintStateService ) {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getBundleId() ).thenReturn( bundleId );
    when( bundle.getState() ).thenReturn( bundleState );
    when( bundle.getSymbolicName() ).thenReturn( WatchersTestUtils.getBundleName( bundleId ) );
    when( blueprintStateService.hasBlueprint( bundleId ) ).thenReturn( hasBlueprint );
    when( blueprintStateService.getBundleState( bundleId ) ).thenReturn( blueprintState );

    when( blueprintStateService.isBlueprintLoaded( bundleId ) ).thenReturn( blueprintState.equals( BundleState.Active ) );

    when( blueprintStateService.isBlueprintFailed( bundleId ) ).thenReturn( blueprintState.equals( BundleState.Failure ) );

    switch ( blueprintState ) {
      case GracePeriod:
      case Waiting:
      case Starting:
        when( blueprintStateService.isBlueprintTryingToLoad( bundleId ) ).thenReturn( Boolean.TRUE );
        break;
      default:
        when( blueprintStateService.isBlueprintTryingToLoad( bundleId ) ).thenReturn( Boolean.FALSE );
    }

    when( blueprintStateService.getBundleMissDependencies( bundleId ) ).thenReturn( dependencies );
    when( blueprintStateService.getBundleFailureCause( bundleId ) ).thenReturn( cause );

    return bundle;
  }
}
