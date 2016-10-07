/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.Dependency;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.osgi.api.IKarafFeatureWatcher.FeatureWatcherException;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;

public class KarafFeatureWatcherImplTest {
  private static final String FEATURES_DELIMITER = ",";
  private static final String PENTAHO_FEATURES_CONDIGURATION_NAME = "org.pentaho.features";
  private static final String RUNTIME_FEATURES_PROPERTY_NAME = "runtimeFeatures";
  private static final String KARAF_FEATURES_CONDIGURATION_NAME = "org.apache.karaf.features";
  private static final String BOOT_FEATURES_PROPERTY_NAME = "featuresBoot";

  private static final String PARENT_ACTIVE_BOOT_FEATURE_NAME = "ParentActiveFeature";
  private static final String CHILD_ACTIVE_BOOT_FEATURE_NAME = "ChildActiveFeature";
  private static final String ACTIVE_RUNTIME_FEATURE_NAME = "ActiveRuntimeFeature";

  private static final String PARENT_FAILED_RUNTIME_FEATURE_NAME = "ParentFailedRunTimeFeature";
  private static final String CHILD_FAILED_RUNTIME_FEATURE_NAME = "ChildFailedRunTimeFeature";
  private static final String PARENT_FAILED_BOOT_FEATURE_NAME = "ParentFailedActiveFeature";
  private static final String CHILD_FAILED_BOOT_FEATURE_NAME = "ChildFailedFeature";

  private static final String NO_FEATURE_VERSION = null;
  private static final String CHILD_ACTIVE_BOOT_FEATURE_VERSION = "1.2.3";
  private static final String CHILD_FAILED_RUNTIME_FEATURE_VERSION = "4.5.60";
  private static final String PARENT_FAILED_BOOT_FEATURE_VERSION = "77.9.81";

  private static final List<Dependency> MOCK_FEATURE_NO_DEPENDENCIES = null;
  private static final List<BundleInfo> MOCK_FEATURE_NO_BUNDLES = null;

  private static final long MOCK_BUNDLE_ACTIVE_ID = 3;
  private static final long MOCK_BUNDLE_FAILURE_ID = 6;
  private static final long MOCK_BUNDLE_GRACE_PERIOD_ID = 7;
  private static final long MOCK_BUNDLE_UNKNOWN_ID = 12;

  private static final String[] MOCK_BUNDLE_NO_DEPENDENCIES = null;
  private static final String[] MOCK_BUNDLE_FAILED_DEPENDENCIES = { "dependency1", "dependency2" };
  private static final String[] MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES = { "dependency6" };

  private static final String EXPECTED_REPORT_HEADER = System.lineSeparator() + "--------- Karaf Feature Watcher Report Begin ---------";
  private static final String EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES =
      "\n--------- Karaf Feature Watcher Report Begin ---------\nFeature 'ParentFailedActiveFeature' with version 77.9.81 did not install.\nThe following bundle(s) are not active and they are contained in feature 'ParentFailedActiveFeature'\n\tBundle 'Bundle_6':\n\t\t Bundle State: Failure\n\t\t Bundle ID: 6\n\t\t Unsatisfied Requirements:\n\t\t\tdependency1\n\t\t\tdependency2\nThe following feature(s) are not active and they are contained in feature 'ParentFailedActiveFeature'\n\tFeature 'ChildFailedFeature' did not install.\n\tThe following bundle(s) are not active and they are contained in feature 'ChildFailedFeature'\n\t\tBundle 'Bundle_12':\n\t\t\t Bundle State: Unknown\n\t\t\t Bundle ID: 12\n--------- Karaf Feature Watcher Report End ---------";
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
  }

  @Test
  public void testWaitForFeatures() throws Exception {
    // Setting the karaf timeout to 1ms, because this is an unit test
    IApplicationContext applicationContext = mock( IApplicationContext.class );
    PentahoSystem.init( applicationContext );
    when( applicationContext.getProperty( KarafBlueprintWatcherImpl.KARAF_TIMEOUT_PROPERTY ) ).thenReturn( "1" );

    BundleContext bundleContext = mock( BundleContext.class );

    ConfigurationAdmin configurationAdmin = mock( ConfigurationAdmin.class );
    @SuppressWarnings( "unchecked" )
    ServiceReference<ConfigurationAdmin> configurationAdminReference =
        (ServiceReference<ConfigurationAdmin>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( ConfigurationAdmin.class ) ).thenReturn( configurationAdminReference );
    when( bundleContext.getService( configurationAdminReference ) ).thenReturn( configurationAdmin );

    // Runtime Features
    Configuration pentahoFeaturesConfig = mock( Configuration.class );
    Hashtable<String, String> dictionaryRuntimeFeatures = new Hashtable<String, String>();
    when( pentahoFeaturesConfig.getProperties() ).thenReturn( dictionaryRuntimeFeatures );
    when( configurationAdmin.getConfiguration( PENTAHO_FEATURES_CONDIGURATION_NAME ) ).thenReturn(
        pentahoFeaturesConfig );

    @SuppressWarnings( "unchecked" )
    ServiceReference<FeaturesService> featuresServiceReference =
        (ServiceReference<FeaturesService>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( FeaturesService.class ) ).thenReturn( featuresServiceReference );
    FeaturesService featuresService = mock( FeaturesService.class );
    when( bundleContext.getService( featuresServiceReference ) ).thenReturn( featuresService );

    @SuppressWarnings( "unchecked" )
    ServiceReference<BundleService> bundleServiceReference =
        (ServiceReference<BundleService>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( BundleService.class ) ).thenReturn( bundleServiceReference );
    BundleService bundleService = mock( BundleService.class );
    when( bundleContext.getService( bundleServiceReference ) ).thenReturn( bundleService );

    List<String> runTimeFeatures = new ArrayList<String>();
    runTimeFeatures.add( ACTIVE_RUNTIME_FEATURE_NAME );
    Dependency activeRuntimeFeature =
        createMockFeature( ACTIVE_RUNTIME_FEATURE_NAME, NO_FEATURE_VERSION, true, MOCK_FEATURE_NO_DEPENDENCIES,
            MOCK_FEATURE_NO_BUNDLES, featuresService );

    dictionaryRuntimeFeatures.put( RUNTIME_FEATURES_PROPERTY_NAME, StringUtils.join( runTimeFeatures,
        FEATURES_DELIMITER ) );

    List<String> bootFeatures = new ArrayList<String>();
    Dependency childActiveFeature =
        createMockFeature( CHILD_ACTIVE_BOOT_FEATURE_NAME, CHILD_ACTIVE_BOOT_FEATURE_VERSION, true,
            MOCK_FEATURE_NO_DEPENDENCIES, MOCK_FEATURE_NO_BUNDLES, featuresService );

    bootFeatures.add( PARENT_ACTIVE_BOOT_FEATURE_NAME );
    List<Dependency> parentActiveDependencies = new ArrayList<Dependency>();
    parentActiveDependencies.add( childActiveFeature );
    createMockFeature( PARENT_ACTIVE_BOOT_FEATURE_NAME, NO_FEATURE_VERSION, true, parentActiveDependencies,
        MOCK_FEATURE_NO_BUNDLES, featuresService );

    Configuration karafFeaturesConfig = mock( Configuration.class );
    Hashtable<String, String> dictionaryBootFeatures = new Hashtable<String, String>();
    dictionaryBootFeatures.put( BOOT_FEATURES_PROPERTY_NAME, StringUtils.join( bootFeatures, FEATURES_DELIMITER ) );
    when( karafFeaturesConfig.getProperties() ).thenReturn( dictionaryBootFeatures );
    when( configurationAdmin.getConfiguration( KARAF_FEATURES_CONDIGURATION_NAME ) ).thenReturn( karafFeaturesConfig );

    // Test with active bundles only

    KarafFeatureWatcherImpl karafFeatureWatcherImpl = new KarafFeatureWatcherImpl( bundleContext );

    try {
      karafFeatureWatcherImpl.waitForFeatures();
    } catch ( FeatureWatcherException e ) {
      throw e;
    }

    Assert.assertTrue( WatchersTestUtils.findKarafDebugOutput( messages, EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES ) == null );

    // RuntimeFeatures
    List<BundleInfo> childFailedRuntimeBundles = new ArrayList<BundleInfo>();
    childFailedRuntimeBundles.add( createMockBundle( MOCK_BUNDLE_GRACE_PERIOD_ID, BundleState.GracePeriod,
        MOCK_BUNDLE_GRACE_PERIOD_DEPENDENCIES, bundleContext, bundleService ) );
    childFailedRuntimeBundles.add( createMockBundle( MOCK_BUNDLE_ACTIVE_ID, BundleState.Active,
        MOCK_BUNDLE_NO_DEPENDENCIES, bundleContext, bundleService ) );

    Dependency failedDependency =
        createMockFeature( CHILD_FAILED_RUNTIME_FEATURE_NAME, CHILD_FAILED_RUNTIME_FEATURE_VERSION, false,
            MOCK_FEATURE_NO_DEPENDENCIES, childFailedRuntimeBundles, featuresService );
    List<Dependency> parentFailedRunTimeDependencies = new ArrayList<Dependency>();
    parentFailedRunTimeDependencies.add( failedDependency );
    parentFailedRunTimeDependencies.add( activeRuntimeFeature );
    createMockFeature( PARENT_FAILED_RUNTIME_FEATURE_NAME, NO_FEATURE_VERSION, false, parentFailedRunTimeDependencies,
        MOCK_FEATURE_NO_BUNDLES, featuresService );
    runTimeFeatures.add( PARENT_FAILED_RUNTIME_FEATURE_NAME );

    // BootFeatures
    List<BundleInfo> childFailedBootBundles = new ArrayList<BundleInfo>();
    childFailedBootBundles.add( createMockBundle( MOCK_BUNDLE_UNKNOWN_ID, BundleState.Unknown,
        MOCK_BUNDLE_NO_DEPENDENCIES, bundleContext, bundleService ) );

    Dependency failedBootDependency =
        createMockFeature( CHILD_FAILED_BOOT_FEATURE_NAME, NO_FEATURE_VERSION, false, MOCK_FEATURE_NO_DEPENDENCIES,
            childFailedBootBundles, featuresService );
    List<Dependency> parentFailedBootDependencies = new ArrayList<Dependency>();
    parentFailedBootDependencies.add( failedBootDependency );

    List<BundleInfo> parentFailedBootBundles = new ArrayList<BundleInfo>();
    parentFailedBootBundles.add( createMockBundle( MOCK_BUNDLE_FAILURE_ID, BundleState.Failure,
        MOCK_BUNDLE_FAILED_DEPENDENCIES, bundleContext, bundleService ) );

    createMockFeature( PARENT_FAILED_BOOT_FEATURE_NAME, PARENT_FAILED_BOOT_FEATURE_VERSION, false,
        parentFailedBootDependencies, parentFailedBootBundles, featuresService );
    bootFeatures.add( PARENT_FAILED_BOOT_FEATURE_NAME );

    dictionaryRuntimeFeatures.put( RUNTIME_FEATURES_PROPERTY_NAME, StringUtils.join( runTimeFeatures,
        FEATURES_DELIMITER ) );
    dictionaryBootFeatures.put( BOOT_FEATURES_PROPERTY_NAME, StringUtils.join( bootFeatures, FEATURES_DELIMITER ) );

    try {
      karafFeatureWatcherImpl.waitForFeatures();
    } catch ( FeatureWatcherException e ) {
      Throwable cause = e.getCause();
      Assert.assertTrue( cause instanceof IKarafFeatureWatcher.FeatureWatcherException );

      String message = e.getCause().getMessage();
      Assert.assertTrue( message.contains( PARENT_FAILED_BOOT_FEATURE_NAME ) );

      String debugOutputNotInstalledFeatures = WatchersTestUtils.findKarafDebugOutput( messages, EXPECTED_REPORT_HEADER );
      if ( debugOutputNotInstalledFeatures == null ) {
        Assert.fail();
      }
      WatchersTestUtils.testEquivalentReports( debugOutputNotInstalledFeatures, EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES );
      return;
    }
    Assert.fail();
  }

  private Dependency createMockFeature( String name, String version, boolean installed, List<Dependency> dependencies,
      List<BundleInfo> bundles, FeaturesService featuresService ) throws Exception {
    Feature feature = mock( Feature.class );
    when( feature.getName() ).thenReturn( name );

    when( featuresService.getFeature( name ) ).thenReturn( feature );
    if ( version != null ) {
      when( feature.hasVersion() ).thenReturn( true );
      when( feature.getVersion() ).thenReturn( version );
      when( featuresService.getFeature( name, version ) ).thenReturn( feature );

    } else {
      when( feature.hasVersion() ).thenReturn( false );
    }

    when( feature.getDependencies() ).thenReturn( dependencies );
    when( feature.getBundles() ).thenReturn( bundles );

    when( featuresService.isInstalled( feature ) ).thenReturn( installed );

    Dependency dependency = mock( Dependency.class );
    when( dependency.getName() ).thenReturn( name );
    when( dependency.getVersion() ).thenReturn( version );
    return dependency;
  }

  private BundleInfo createMockBundle( long bundleId, BundleState bundleState, String[] missingDependencies,
      BundleContext bundleContext, BundleService bundleService ) {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getBundleId() ).thenReturn( bundleId );
    String bundleName = WatchersTestUtils.getBundleName( bundleId );
    when( bundle.getSymbolicName() ).thenReturn( bundleName );
    org.apache.karaf.bundle.core.BundleInfo bundleInfo = mock( org.apache.karaf.bundle.core.BundleInfo.class );
    when( bundleInfo.getState() ).thenReturn( bundleState );
    when( bundleService.getInfo( bundle ) ).thenReturn( bundleInfo );

    List<BundleRequirement> unsatisfiedRquirements = null;
    if ( missingDependencies != null ) {
      unsatisfiedRquirements = new ArrayList<BundleRequirement>();
      for ( String missingDependency : missingDependencies ) {
        BundleRequirement requirement = mock( BundleRequirement.class );
        when( requirement.toString() ).thenReturn( missingDependency );
        unsatisfiedRquirements.add( requirement );
      }
    }
    when( bundleService.getUnsatisfiedRquirements( bundle, null ) ).thenReturn( unsatisfiedRquirements );

    BundleInfo featureBundleInfo = mock( BundleInfo.class );
    when( featureBundleInfo.getLocation() ).thenReturn( bundleName );
    when( bundleContext.getBundle( bundleName ) ).thenReturn( bundle );
    return featureBundleInfo;
  }



}
