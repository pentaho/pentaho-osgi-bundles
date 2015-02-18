package org.pentaho.osgi.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafFeatureWatcherImpl implements IKarafFeatureWatcher {
  private BundleContext bundleContext;
  private long timeout = 60 * 1000L;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public KarafFeatureWatcherImpl( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
  }

  @Override public void waitForFeatures() throws FeatureWatcherException {

    long entryTime = System.currentTimeMillis();

    ServiceReference<FeaturesService> serviceReference = bundleContext.getServiceReference( FeaturesService.class );
    FeaturesService featuresService = bundleContext.getService( serviceReference );

    ServiceReference<ConfigurationAdmin> serviceReference1 =
        bundleContext.getServiceReference( ConfigurationAdmin.class );
    ConfigurationAdmin configurationAdmin = bundleContext.getService( serviceReference1 );

    try {
      Configuration configuration = configurationAdmin.getConfiguration( "org.apache.karaf.features" );
      String featuresBoot = (String) configuration.getProperties().get( "featuresBoot" );
      String[] requiredFeatures = featuresBoot.split( "," );

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
