/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.bundle.core.BundleStateService;
import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.Dependency;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.hadoop.shim.DriverManager;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafFeatureWatcherImpl implements IKarafFeatureWatcher {
  private BundleContext bundleContext;
  private long timeout;
  private Logger logger = LoggerFactory.getLogger( getClass() );
  private static final String KARAF_TIMEOUT_PROPERTY = "karafWaitForBoot";

  public KarafFeatureWatcherImpl( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
    // Default timeout of 2 minutes can be overridden in server.properties
    timeout =
        PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) == null ? 2 * 60 * 1000L : Long
            .valueOf( PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) );
  }

  @Override public void waitForFeatures() throws FeatureWatcherException {

    try {
      List<String> bootFeatures = this.getFeatures( "org.apache.karaf.features", "featuresBoot" );
      waitForFeatures( bootFeatures );

      List<String> runtimeFeatures = this.getFeatures( "org.pentaho.features",  "runtimeFeatures" );
      // Install extra features
      ICapabilityManager manager = DefaultCapabilityManager.getInstance();
      if ( manager != null ) {
        for ( String runtimeFeature : runtimeFeatures ) {
          ICapability capability = manager.getCapabilityById( runtimeFeature );
          if ( capability != null && !capability.isInstalled() ) {
            capability.install();
          }
        }
      }
      waitForFeatures( runtimeFeatures );

      if ( getBooleanProperty( DriverManager.CONFIG_FILE_NAME,  DriverManager.INSTALL_DRIVERS_PROPERTY ) ) {
        DriverManager.getInstance( bundleContext ).installDrivers();
      }

    } catch ( IOException e ) {
      throw new FeatureWatcherException( "Error accessing ConfigurationAdmin", e );
    } catch ( Exception e ) {
      throw new FeatureWatcherException( "Unknown error in KarafWatcher", e );
    }
  }


  private void waitForFeatures( List<String> requiredFeatures ) throws Exception {
    FeaturesService featuresService = this.getFeatureService();
    if ( featuresService == null ) {
      logger.warn( "Unable to get FeatureService to start waiting for features." );
      return;
    }

    long entryTime = System.currentTimeMillis();
    // Loop through to see if features are all installed
    while ( true ) {

      List<String> uninstalledFeatures = new ArrayList<String>();

      for ( String requiredFeature : requiredFeatures ) {
        requiredFeature = requiredFeature.trim();
        Feature feature = featuresService.getFeature( requiredFeature );
        if ( feature != null && featuresService.isInstalled( feature ) == false ) {
          uninstalledFeatures.add( requiredFeature );
        }
      }
      if ( uninstalledFeatures.size() > 0 ) {
        if ( System.currentTimeMillis() - timeout > entryTime ) {
          IServiceBarrier serviceBarrier =
              IServiceBarrierManager.LOCATOR.getManager().getServiceBarrier( "KarafFeatureWatcherBarrier" );
          if ( serviceBarrier == null || serviceBarrier.isAvailable() ) {
            if ( logger.isDebugEnabled() ) {
              logger.debug( getFeaturesReport( featuresService, uninstalledFeatures ) );
            }
            throw new FeatureWatcherException( "Timed out waiting for Karaf features to install: " + StringUtils
                .join( uninstalledFeatures, "," ) );
          } else {
            entryTime = System.currentTimeMillis();
          }
        }
        if ( logger.isDebugEnabled() ) {
          logger.debug( "KarafFeatureWatcher is waiting for the following features to install: " + StringUtils.join(
            uninstalledFeatures, "," ) );
        }
        Thread.sleep( 100 );
        continue;
      }
      break;
    }
  }

  private FeaturesService getFeatureService() {
    if ( this.featuresService == null ) {
      // Start the serviceTracker timer
      ServiceTracker serviceTracker = new ServiceTracker( bundleContext, FeaturesService.class.getName(), null );
      serviceTracker.open();
      try {
        serviceTracker.waitForService( timeout );
      } catch ( InterruptedException e ) {
        logger.debug( "FeaturesService " + FeaturesService.class.getName() + " ServiceTracker Interrupted" );
      } finally {
        serviceTracker.close();
      }

      ServiceReference<FeaturesService> featureServiceReference = bundleContext.getServiceReference( FeaturesService.class );
      if ( featureServiceReference != null ) {
        this.featuresService = bundleContext.getService( featureServiceReference );
      }
    }

    return this.featuresService;
  }
  private FeaturesService featuresService;

  private ConfigurationAdmin getConfigurationAdmin() {
    if ( this.configurationAdmin == null ) {
      ServiceReference<ConfigurationAdmin> configurationAdminServiceReference =
        bundleContext.getServiceReference( ConfigurationAdmin.class );
      this.configurationAdmin = bundleContext.getService( configurationAdminServiceReference );
    }

    return this.configurationAdmin;
  }
  private ConfigurationAdmin configurationAdmin;

  /**
   * Gets a boolean from a property in a configuration.
   * @param configPersistentId The persistent id of the Configuration.
   * @param featuresPropertyKey The property key where the features are declared in the Configuration.
   * @return the boolean value of the requested property,
   *         a false value if no Configuration for the persistentId is found,
   *         a false value if the features property key is not mapped.
   *         a false value if the features property has a non-boolean value.
   * @throws IOException if access to persistent storage fails.
   */
  private boolean getBooleanProperty( String configPersistentId, String featuresPropertyKey ) throws IOException {
    Configuration configuration = this.getConfigurationAdmin().getConfiguration( configPersistentId );

    Dictionary<String, Object> properties = configuration.getProperties();
    if ( properties == null ) {
      return false;
    }

    String featuresPropertyValue = (String) properties.get( featuresPropertyKey );
    if ( featuresPropertyValue == null ) {
      return false;
    }

    return Boolean.parseBoolean( featuresPropertyValue );
  }

  /**
   * Gets features from a property in a configuration.
   * @param configPersistentId The persistent id of the Configuration.
   * @param featuresPropertyKey The property key where the features are declared in the Configuration.
   * @return an list with requested features,
   *         an empty list if no Configuration for the persistentId is found,
   *         an empty list if the features property key is not mapped.
   * @throws IOException if access to persistent storage fails.
   */
  protected List<String> getFeatures( String configPersistentId, String featuresPropertyKey ) throws IOException {
    Configuration configuration = this.getConfigurationAdmin().getConfiguration( configPersistentId );

    Dictionary<String, Object> properties = configuration.getProperties();
    if ( properties == null ) {
      return Collections.emptyList();
    }

    String featuresPropertyValue = (String) properties.get( featuresPropertyKey );
    if ( featuresPropertyValue == null ) {
      return Collections.emptyList();
    }

    // remove parentesis from feature stages
    featuresPropertyValue = featuresPropertyValue.replaceAll( "[()]", "" );
    if ( featuresPropertyValue.length() == 0 ) {
      return Collections.emptyList();
    }

    String[] featuresArray = featuresPropertyValue.split( "," );
    return Arrays.asList( featuresArray );
  }


  // All features report
  private String getFeaturesReport( FeaturesService featuresService, List<String> uninstalledFeatures )
    throws Exception {
    ServiceReference<BundleService> serviceReferenceBundleService =
        bundleContext.getServiceReference( BundleService.class );
    BundleService bundleService = bundleContext.getService( serviceReferenceBundleService );
    List<BundleStateService> bundleStateServices = getBundleStateServices();

    String featuresReport = System.lineSeparator() + "--------- Karaf Feature Watcher Report Begin ---------";
    for ( String uninstalledFeature : uninstalledFeatures ) {
      Feature feature = featuresService.getFeature( uninstalledFeature );
      featuresReport +=
          System.lineSeparator() + getFeatureReport( featuresService, bundleService, bundleStateServices, feature );

    }
    return featuresReport + System.lineSeparator() + "--------- Karaf Feature Watcher Report End ---------";
  }

  // Single feature report
  private String getFeatureReport( FeaturesService featuresService, BundleService bundleService,
      List<BundleStateService> bundleStateServices, Feature feature ) throws Exception {
    String featureReport = "";
    if ( feature.hasVersion() ) {
      featureReport += "Feature '" + feature.getName() + "' with version " + feature.getVersion() + " did not install.";
    } else {
      featureReport += "Feature '" + feature.getName() + "' did not install.";
    }

    // For this feature, we list its non active bundles with additional information
    if ( null == bundleService ) {
      featureReport += "Cannot list bundles; bundleService not available";
    } else if ( feature.getBundles() != null ) {
      boolean first = true;
      for ( BundleInfo bundleInfo : feature.getBundles() ) {
        Bundle bundle = bundleContext.getBundle( bundleInfo.getLocation() );
        if ( null == bundle ) {
          featureReport += System.lineSeparator() + "Bundle for " + bundleInfo.getLocation() + " is null.";
        } else if ( bundleService.getInfo( bundle ).getState() != BundleState.Active ) {
          if ( first == true ) {
            featureReport +=
                System.lineSeparator() + "The following bundle(s) are not active and they are contained in feature '"
                    + feature.getName() + "'";
            first = false;
          }

          featureReport +=
              System.lineSeparator() + "\t" + getBundleReport( bundleService, bundleStateServices, bundle ).replaceAll(
                  "\n", "\n\t" );
        }
      }
    }

    // For this feature, we list its non installed features in a higher indent level
    if ( feature.getDependencies() != null ) {
      boolean first = true;
      for ( Dependency dependency : feature.getDependencies() ) {
        String dependencyName = dependency.getName();
        String dependencyVersion = dependency.getVersion();
        Feature dependencyFeature;
        if ( dependencyVersion != null && dependencyVersion.isEmpty() == false ) {
          dependencyFeature = featuresService.getFeature( dependencyName, dependencyVersion );
        } else {
          dependencyFeature = featuresService.getFeature( dependencyName );
        }

        if ( dependencyFeature != null && featuresService.isInstalled( dependencyFeature ) == false ) {
          if ( first == true ) {
            featureReport +=
                System.lineSeparator() + "The following feature(s) are not active and they are contained in feature '"
                    + feature.getName() + "'";
            first = false;
          }

          featureReport +=
              System.lineSeparator() + "\t" + getFeatureReport( featuresService, bundleService, bundleStateServices,
                  dependencyFeature ).replaceAll( "\n", "\n\t" );
        }
        first = false;
      }
    }
    return featureReport;
  }

  private List<BundleStateService> getBundleStateServices() throws InvalidSyntaxException {
    List<BundleStateService> bundleStateServices = new ArrayList<BundleStateService>();
    Collection<ServiceReference<BundleStateService>> serviceReferenceBundleStateService =
        bundleContext.getServiceReferences( BundleStateService.class, null );

    for ( ServiceReference<BundleStateService> bundleStateService : serviceReferenceBundleStateService ) {
      bundleStateServices.add( bundleContext.getService( bundleStateService ) );
    }

    return bundleStateServices;
  }

  // Single bundle report
  private String getBundleReport( BundleService bundleService, List<BundleStateService> bundleStateServices,
      Bundle bundle ) {
    BundleState bundleState = bundleService.getInfo( bundle ).getState();
    long bundleId = bundle.getBundleId();
    String bundleName = bundle.getSymbolicName();

    String bundleReport =
        "Bundle '" + bundleName + "':" + System.lineSeparator() + "\t Bundle State: " + bundleState + System
            .lineSeparator() + "\t Bundle ID: " + bundleId;
    // We loop through the available Bundle State Services and gather diagnostic information, if it exists. Usually,
    // there are two Bundle State Services, the BlueprintStateService and the SpringStateService.
    for ( BundleStateService bundleStateService : bundleStateServices ) {
      String part = bundleStateService.getDiag( bundle );
      if ( part != null ) {
        bundleReport += bundleStateService.getName() + "\n";
        bundleReport += part.replaceAll( "\n", "\n\t" );
      }
    }

    // Unsatisfied Requirements for this bundle, includes optional requirements
    List<BundleRequirement> missingDependencies = bundleService.getUnsatisfiedRequirements( bundle, null );
    if ( missingDependencies != null && missingDependencies.isEmpty() == false ) {
      bundleReport += System.lineSeparator() + "\t Unsatisfied Requirements:";
      for ( BundleRequirement missDependency : missingDependencies ) {
        bundleReport += System.lineSeparator() + "\t\t" + missDependency;
      }
    }

    return bundleReport;
  }
}
