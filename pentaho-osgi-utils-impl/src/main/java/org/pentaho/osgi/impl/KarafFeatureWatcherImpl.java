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
 * Copyright 2015-2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.osgi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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


    // Start the serviceTracker timer
    ServiceTracker serviceTracker = new ServiceTracker( bundleContext, FeaturesService.class.getName(), null );
    serviceTracker.open();
    try {
      serviceTracker.waitForService( timeout );
    } catch ( InterruptedException e ) {
      logger.debug( "FeaturesService " + FeaturesService.class.getName() + " ServiceTracker Interrupted" );
    }

    ServiceReference<FeaturesService> serviceReference = bundleContext.getServiceReference( FeaturesService.class );
    if ( serviceReference != null ) {
      FeaturesService featuresService = bundleContext.getService( serviceReference );

      ServiceReference<ConfigurationAdmin> serviceReference1 =
          bundleContext.getServiceReference( ConfigurationAdmin.class );
      ConfigurationAdmin configurationAdmin = bundleContext.getService( serviceReference1 );

      try {
        List<String> requiredFeatures = new ArrayList<String>();

        Configuration configuration = configurationAdmin.getConfiguration( "org.apache.karaf.features" );
        String featuresBoot = (String) configuration.getProperties().get( "featuresBoot" );
        String[] fs = featuresBoot.split( "," );
        requiredFeatures.addAll( Arrays.asList( fs ) );

        waitForFeatures( requiredFeatures, featuresService );


        List<String> extraFeatures = new ArrayList<String>();
        // Install extra features
        configuration = configurationAdmin.getConfiguration( "org.pentaho.features" );
        if ( configuration != null && configuration.getProperties() != null ) {
          String extraFeaturesStr = (String) configuration.getProperties().get( "runtimeFeatures" );
          if ( extraFeaturesStr != null ) {
            fs = extraFeaturesStr.split( "," );
            extraFeatures.addAll( Arrays.asList( fs ) );
          }
          ICapabilityManager manager = DefaultCapabilityManager.getInstance();
          if ( manager != null ) {
            for ( String extraFeature : extraFeatures ) {
              ICapability capability = manager.getCapabilityById( extraFeature );
              if ( capability != null && !capability.isInstalled() ) {
                capability.install();
              }
            }
          }
        }

        waitForFeatures( extraFeatures, featuresService );


      } catch ( IOException e ) {
        throw new FeatureWatcherException( "Error accessing ConfigurationAdmin", e );
      } catch ( Exception e ) {
        throw new FeatureWatcherException( "Unknown error in KarafWatcher", e );
      }
    }
  }

  private void waitForFeatures( List<String> requiredFeatures, FeaturesService featuresService ) throws Exception {

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
            logger.debug( getFeaturesReport( featuresService, uninstalledFeatures ) );
            throw new FeatureWatcherException( "Timed out waiting for Karaf features to install: " + StringUtils
                .join( uninstalledFeatures, "," ) );
          } else {
            entryTime = System.currentTimeMillis();
          }
        }
        logger.debug( "KarafFeatureWatcher is waiting for the following features to install: " + StringUtils.join(
            uninstalledFeatures, "," ) );
        Thread.sleep( 100 );
        continue;
      }
      break;
    }
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
    if ( feature.getBundles() != null ) {
      boolean first = true;
      for ( BundleInfo bundleInfo : feature.getBundles() ) {
        Bundle bundle = bundleContext.getBundle( bundleInfo.getLocation() );
        if ( bundleService.getInfo( bundle ).getState() != BundleState.Active ) {
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
    List<BundleRequirement> missingDependencies = bundleService.getUnsatisfiedRquirements( bundle, null );
    if ( missingDependencies != null && missingDependencies.isEmpty() == false ) {
      bundleReport += System.lineSeparator() + "\t Unsatisfied Requirements:";
      for ( BundleRequirement missDependency : missingDependencies ) {
        bundleReport += System.lineSeparator() + "\t\t" + missDependency;
      }
    }

    return bundleReport;
  }
}
