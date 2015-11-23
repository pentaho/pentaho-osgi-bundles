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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
    // Default timeout to 4 hours per BACKLOG-5526.  Can be overridden in server.properties
    timeout =
        PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) == null ? 4 * 60 * 60 * 1000L : Long
            .valueOf( PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) );
  }

  @Override public void waitForFeatures() throws FeatureWatcherException {

    long entryTime = System.currentTimeMillis();

    ServiceTracker serviceTracker = new ServiceTracker( bundleContext, FeaturesService.class.getName(), null );
    serviceTracker.open();
    try {
      serviceTracker.waitForService( timeout );
    } catch ( InterruptedException e ) {
      logger.debug( "FeaturesService " + FeaturesService.class.getName() + " ServiceTracker Interrupted" );
    }

    ServiceReference<FeaturesService> serviceReference = bundleContext.getServiceReference( FeaturesService.class );
    if(serviceReference != null) {
    FeaturesService featuresService = bundleContext.getService( serviceReference );

    ServiceReference<ConfigurationAdmin> serviceReference1 =
        bundleContext.getServiceReference( ConfigurationAdmin.class );
    ConfigurationAdmin configurationAdmin = bundleContext.getService( serviceReference1 );


    try {
      List<String> requiredFeatures = new ArrayList<String>();

      // Install extra features
      Configuration configuration = configurationAdmin.getConfiguration( "org.pentaho.features" );
      if( configuration != null && configuration.getProperties() != null ) {
        String extraFeatures = (String) configuration.getProperties().get( "runtimeFeatures" );
        if( extraFeatures != null ) {
          String[] fs = extraFeatures.split( "," );
          requiredFeatures.addAll( Arrays.asList( fs ) );
        }
        ICapabilityManager manager= DefaultCapabilityManager.getInstance();
        if( manager != null ) {
          for ( String requiredFeature : requiredFeatures ) {
            ICapability capability = manager.getCapabilityById( requiredFeature );
            if ( capability != null ) {
              capability.install();
            }
          }
        }
      }

      configuration = configurationAdmin.getConfiguration( "org.apache.karaf.features" );
      String featuresBoot = (String) configuration.getProperties().get( "featuresBoot" );
      String[] fs = featuresBoot.split( "," );
      requiredFeatures.addAll( Arrays.asList( fs ) );

      // Loop through to see if features are all installed
      outer:
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
            throw new FeatureWatcherException( "Timed out waiting for Karaf features to install: " + StringUtils.join
                ( uninstalledFeatures, "," ) );
          }
          logger.debug( "KarafFeatureWatcher is waiting for the following features to install: " + StringUtils.join
              ( uninstalledFeatures, "," ) );
          Thread.sleep( 100 );
          continue;
        }
        break;
      }

    } catch ( IOException e ) {
      throw new FeatureWatcherException( "Error accessing ConfigurationAdmin", e );
    } catch ( Exception e ) {
      throw new FeatureWatcherException( "Unknown error in KarafWatcher", e );
    }
    } 
  }
}
