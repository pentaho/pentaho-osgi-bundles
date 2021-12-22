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

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.Dependency;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.layout.PatternLayout;
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
import org.junit.After;
import org.junit.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;

public class  KarafFeatureWatcherImplTest {
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
    "--------- Karaf Feature Watcher Report Begin ---------" + System.lineSeparator()
    + "Feature 'ParentFailedActiveFeature' with version 77.9.81 did not install." + System.lineSeparator()
    + "The following bundle(s) are not active and they are contained in feature 'ParentFailedActiveFeature'" + System.lineSeparator()
    + "\tBundle 'Bundle_6':" + System.lineSeparator()
    + "\t\t Bundle State: Failure" + System.lineSeparator()
    + "\t\t Bundle ID: 6" + System.lineSeparator()
    + "\t\t Unsatisfied Requirements:" + System.lineSeparator()
    + "\t\t\tdependency1" + System.lineSeparator()
    + "\t\t\tdependency2" + System.lineSeparator()
    + "The following feature(s) are not active and they are contained in feature 'ParentFailedActiveFeature'" + System.lineSeparator()
    + "\tFeature 'ChildFailedFeature' did not install." + System.lineSeparator()
    + "\tThe following bundle(s) are not active and they are contained in feature 'ChildFailedFeature'" + System.lineSeparator()
    + "\t\tBundle 'Bundle_12':" + System.lineSeparator()
    + "\t\t\t Bundle State: Unknown" + System.lineSeparator()
    + "\t\t\t Bundle ID: 12" + System.lineSeparator()
    + "--------- Karaf Feature Watcher Report End ---------";

  private StringWriter sw;
  private Appender appender;

  @Before
  public void setUp() {
    sw = new StringWriter();
    appender = LogUtil.makeAppender(
      "KarafFeatureWatcherImplTest",
      sw,
      PatternLayout.createDefaultLayout().getConversionPattern());
    LogUtil.addAppender(
      appender, LogManager.getRootLogger(), Level.DEBUG);
    LogUtil.setLoggerLevel(LogManager.getRootLogger(), Level.DEBUG);

    // Setting the karaf timeout to 1ms, because this is an unit test
    IApplicationContext applicationContext = mock( IApplicationContext.class );
    PentahoSystem.init( applicationContext );
    when( applicationContext.getProperty( KarafBlueprintWatcherImpl.KARAF_TIMEOUT_PROPERTY ) ).thenReturn( "1" );
  }

  @After
  public void tearDown() throws Exception {
    LogUtil.removeAppender(appender, LogManager.getRootLogger());
  }

  @Test
  public void testWaitForFeatures() throws Exception {
    BundleContext bundleContext = mock( BundleContext.class );

    ConfigurationAdmin configurationAdmin = mock( ConfigurationAdmin.class );
    @SuppressWarnings( "unchecked" )
    ServiceReference<ConfigurationAdmin> configurationAdminReference =
        (ServiceReference<ConfigurationAdmin>) mock( ServiceReference.class );
    when( bundleContext.getServiceReference( ConfigurationAdmin.class ) ).thenReturn( configurationAdminReference );
    when( bundleContext.getService( configurationAdminReference ) ).thenReturn( configurationAdmin );

    // Runtime Features
    Configuration pentahoFeaturesConfig = mock( Configuration.class );
    Hashtable<String, Object> dictionaryRuntimeFeatures = new Hashtable();
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
    Hashtable<String, Object> dictionaryBootFeatures = new Hashtable();
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

    String output = sw.toString();
    sw.flush();
    if (output.contains(EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES)) {
      Assert.fail("Expected string not found");
    }

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

      output = sw.toString();
      sw.flush();
      if (!output.contains(EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES)) {
        Assert.assertEquals(EXPECTED_REPORT_FOR_NOT_INSTALLED_FEATURES, output);
        Assert.fail("Expected string not found");
      }
      return;
    }
    Assert.fail();
  }

  @Test
  public void testGetFeaturesNoFeatures() throws Exception {
    String featuresPropertyValue = "";
    String featuresPropertyKey = "featuresBoot";
    String configurationPid = "org.apache.features";

    KarafFeatureWatcherImpl karafFeatureWatcherImpl = this.createKarafFeatureWatcherImplWithConfig( configurationPid,
      featuresPropertyKey, featuresPropertyValue );

    // Act
    List<String> actualFeatures = karafFeatureWatcherImpl.getFeatures( configurationPid, featuresPropertyKey );

    // Assert
    assertThat( actualFeatures, is( empty() ) );
  }

  @Test
  public void testGetFeaturesWithParentesisStages() throws Exception {
    String featuresPropertyValue = "feature1,(feature2,feature3),feature4";
    String featuresPropertyKey = "featuresBoot";
    String configurationPid = "org.apache.features";

    KarafFeatureWatcherImpl karafFeatureWatcherImpl = this.createKarafFeatureWatcherImplWithConfig( configurationPid,
      featuresPropertyKey, featuresPropertyValue );

    // Act
    List<String> actualFeatures = karafFeatureWatcherImpl.getFeatures( configurationPid, featuresPropertyKey );

    // Assert
    List<String> expectedFeatures = Arrays.asList( "feature1", "feature2", "feature3", "feature4" );
    assertEquals( expectedFeatures, actualFeatures );
  }

  @Test
  public void testGetFeaturesNoParentesisStages() throws Exception {
    String featuresPropertyValue = "feature1,feature2,feature3,feature4";
    String featuresPropertyKey = "featuresBoot";
    String configurationPid = "org.apache.features";

    KarafFeatureWatcherImpl karafFeatureWatcherImpl = this.createKarafFeatureWatcherImplWithConfig( configurationPid,
      featuresPropertyKey, featuresPropertyValue );

    // Act
    List<String> actualFeatures = karafFeatureWatcherImpl.getFeatures( configurationPid, featuresPropertyKey );

    // Assert
    List<String> expectedFeatures = Arrays.asList( "feature1", "feature2", "feature3", "feature4" );
    Assert.assertEquals( expectedFeatures, actualFeatures );
  }


  /**
   * Creates a KarafFeatureWatcherImpl setup with mock configuration.
   * @param configurationPid The persistent id of the Configuration.
   * @param featuresPropertyKey The key of the property to add to the configuration.
   * @param featuresPropertyValue The value of the property to add to the configuration.
   * @return
   * @throws IOException
   */
  private KarafFeatureWatcherImpl createKarafFeatureWatcherImplWithConfig( String configurationPid,
                                                                            String featuresPropertyKey,
                                                                            String featuresPropertyValue )
    throws IOException {
    BundleContext bundleContext = mock( BundleContext.class );
    ConfigurationAdmin configurationAdmin = createMockConfigurationAdmin( bundleContext );

    Dictionary<String, Object> properties = new Hashtable<>();
    properties.put( featuresPropertyKey, featuresPropertyValue );
    addConfiguration( configurationAdmin,  configurationPid, properties );

    return new KarafFeatureWatcherImpl( bundleContext );
  }

  private ConfigurationAdmin createMockConfigurationAdmin( BundleContext bundleContextMock ) {
    ConfigurationAdmin configurationAdmin = mock( ConfigurationAdmin.class );
    @SuppressWarnings( "unchecked" )
    ServiceReference<ConfigurationAdmin> configurationAdminReference =
      (ServiceReference<ConfigurationAdmin>) mock( ServiceReference.class );
    when( bundleContextMock.getServiceReference( ConfigurationAdmin.class ) ).thenReturn( configurationAdminReference );
    when( bundleContextMock.getService( configurationAdminReference ) ).thenReturn( configurationAdmin );

    return configurationAdmin;
  }

  private void addConfiguration( ConfigurationAdmin configurationAdminMock, String configurationPid, Dictionary<String,Object> properties )
    throws IOException {
    // Runtime Features
    Configuration configuration = mock( Configuration.class );
    when( configuration.getProperties() ).thenReturn( properties );
    when( configurationAdminMock.getConfiguration( configurationPid ) ).thenReturn( configuration );
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

    List<BundleRequirement> unsatisfiedRequirements = null;
    if ( missingDependencies != null ) {
      unsatisfiedRequirements = new ArrayList<>();
      for ( String missingDependency : missingDependencies ) {
        BundleRequirement requirement = mock( BundleRequirement.class );
        when( requirement.toString() ).thenReturn( missingDependency );
        unsatisfiedRequirements.add( requirement );
      }
    }
    when( bundleService.getUnsatisfiedRequirements( bundle, null ) ).thenReturn( unsatisfiedRequirements );

    BundleInfo featureBundleInfo = mock( BundleInfo.class );
    when( featureBundleInfo.getLocation() ).thenReturn( bundleName );
    when( bundleContext.getBundle( bundleName ) ).thenReturn( bundle );
    return featureBundleInfo;
  }



}
