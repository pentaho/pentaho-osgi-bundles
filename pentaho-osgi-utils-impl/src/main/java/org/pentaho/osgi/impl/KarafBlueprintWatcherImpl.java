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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.karaf.bundle.core.BundleState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.osgi.api.BlueprintStateService;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 9/4/15.
 */
public class KarafBlueprintWatcherImpl implements IKarafBlueprintWatcher {
  private BundleContext bundleContext;
  private long timeout;
  private Logger logger = LoggerFactory.getLogger( getClass() );
  public static final String KARAF_TIMEOUT_PROPERTY = "karafWaitForBoot";

  public KarafBlueprintWatcherImpl( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
    // Default timeout of 2 minutes can be overridden in server.properties
    timeout =
      PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) == null ? 2 * 60 * 1000L : Long
        .valueOf( PentahoSystem.getApplicationContext().getProperty( KARAF_TIMEOUT_PROPERTY ) );
  }

  @Override public void waitForBlueprint() throws BlueprintWatcherException {
    long entryTime = System.currentTimeMillis();

    ServiceTracker serviceTracker = new ServiceTracker( bundleContext, BlueprintStateService.class.getName(), null );
    serviceTracker.open();
    try {
      serviceTracker.waitForService( timeout );
    } catch ( InterruptedException e ) {
      logger.debug( "BlueprintStateService ServiceTracker Interrupted" );
    }

    ServiceReference<BlueprintStateService>
      serviceReference =
      bundleContext.getServiceReference( BlueprintStateService.class );

    if ( serviceReference != null ) {
      BlueprintStateService blueprintStateService = bundleContext.getService( serviceReference );
      List<Bundle> unloadedAndFailedBlueprints = new ArrayList<Bundle>();

      try {
        while ( true ) {
          List<String> unloadedBlueprints = new ArrayList<String>();
          for ( Bundle bundle : bundleContext.getBundles() ) {
            if ( bundle.getState() != Bundle.RESOLVED ) {
              // We're only interested in bundles which are resolved, not started or installed. This is because a
              // bundle which should have started but failed will be in the resolved state.
              // We cannot assume an installed bundle is meant to be started and thus wait for it, bundles can be
              // installed and never started (even though with Karaf this would be very strange). The only thing we can
              // reasonably do here is skip non-resolved bundles.
              logger.debug( "Blueprint check was skipped for bundle {} as it's not in the 'Resolved' state",
                bundle.getSymbolicName() );
              continue;
            }
            long bundleId = bundle.getBundleId();
            if ( blueprintStateService.hasBlueprint( bundleId ) ) {
              if ( !blueprintStateService.isBlueprintLoaded( bundleId ) ) {
                unloadedAndFailedBlueprints.add( bundle );
                if ( !blueprintStateService.isBlueprintFailed( bundleId ) && blueprintStateService
                  .isBlueprintTryingToLoad( bundleId ) ) {
                  unloadedBlueprints.add( bundle.getSymbolicName() );
                }
              }
            }
          }
          if ( unloadedBlueprints.size() > 0 ) {
            if ( System.currentTimeMillis() - timeout > entryTime ) {
              IServiceBarrier serviceBarrier =
                IServiceBarrierManager.LOCATOR.getManager().getServiceBarrier( "KarafFeatureWatcherBarrier" );
              if ( serviceBarrier == null || serviceBarrier.isAvailable() ) {
                throw new IKarafBlueprintWatcher.BlueprintWatcherException(
                  "Timed out waiting for blueprints to load: " + StringUtils.join( unloadedBlueprints, "," ) );
              } else {
                entryTime = System.currentTimeMillis(); // reset the time. We are still waiting for barriers
              }
            }
            logger.debug( "KarafBlueprintWatcher is waiting for the following blueprints to load: " + StringUtils
              .join( unloadedBlueprints, "," ) );
            Thread.sleep( 100 );
            unloadedAndFailedBlueprints = new ArrayList<Bundle>();
            continue;
          }
          break;
        }

      } catch ( Exception e ) {
        throw new BlueprintWatcherException( "Unknown error in KarafBlueprintWatcher", e );
      } finally {
        if ( unloadedAndFailedBlueprints.size() > 0 ) {
          logger.debug( System.lineSeparator() + getBlueprintsReport( blueprintStateService,
            unloadedAndFailedBlueprints ) );
        }
        serviceTracker.close();
      }
    }
  }

  private String getBlueprintsReport( BlueprintStateService blueprintStateService, List<Bundle> bundles ) {
    String bundlesReport = "--------- Karaf Blueprint Watcher Report Begin ---------";
    bundlesReport += System.lineSeparator() + "Blueprint Bundle(s) not loaded:";
    for ( Bundle bundle : bundles ) {
      bundlesReport +=
        System.lineSeparator() + "\t" + getBlueprintReport( blueprintStateService, bundle ).replaceAll( "\n",
          "\n \t" );
    }

    return bundlesReport + System.lineSeparator() + "--------- Karaf Blueprint Watcher Report End ---------";
  }

  private String getBlueprintReport( BlueprintStateService blueprintStateService, Bundle bundle ) {
    long bundleId = bundle.getBundleId();
    String bundleName = bundle.getSymbolicName();
    BundleState bundleState = blueprintStateService.getBundleState( bundleId );
    String[] missingDependencies = blueprintStateService.getBundleMissDependencies( bundleId );
    Throwable failureCause = blueprintStateService.getBundleFailureCause( bundleId );

    String bundleReport =
      "Blueprint Bundle '" + bundleName + "':" + System.lineSeparator() + "\t Blueprint Bundle State: " + bundleState
        + System.lineSeparator() + "\t Blueprint Bundle ID: " + bundleId;

    // Report missing dependencies from the blueprint, e.g., a mandatory service that did not start
    if ( missingDependencies != null ) {
      bundleReport += System.lineSeparator() + "\t Missing Dependencies:";
      for ( String missDependency : missingDependencies ) {
        bundleReport += System.lineSeparator() + "\t \t" + missDependency;
      }
    }

    // If exist, we report the failure cause that is a throwable, we attach the full stacktrace into the report
    if ( failureCause != null ) {
      bundleReport +=
        System.lineSeparator() + "\t This blueprint state was caused by: " + System.lineSeparator() + "\t \t"
          + getStackTraceString( failureCause ).replaceAll( "\n", "\n \t \t" );
    }
    return bundleReport;
  }

  private String getStackTraceString( Throwable throwable ) {
    String stackTrace = ExceptionUtils.getStackTrace( throwable );

    // Remove if exists last empty line
    int index = stackTrace.lastIndexOf( "\n" );

    if ( index < 0 ) {
      return stackTrace;
    }

    // Test if empty line
    if ( stackTrace.length() <= index + 1 || stackTrace.substring( index + 1 ).trim().isEmpty() ) {
      String newStackTrace = stackTrace.substring( 0, index );

      // remove windows new line character \r
      if ( newStackTrace.endsWith( "\r" ) ) {
        newStackTrace = newStackTrace.substring( 0, newStackTrace.length() - 1 );
      }
      return newStackTrace;
    }
    return stackTrace;
  }
}
