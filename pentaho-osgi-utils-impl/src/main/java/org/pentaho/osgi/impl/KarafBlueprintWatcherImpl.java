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

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.osgi.api.BlueprintStateService;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.BarrierBeanProcessor;
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
  private static final String KARAF_TIMEOUT_PROPERTY = "karafWaitForBoot";

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

      try {
        while ( true ) {
          List<String> unloadedBlueprints = new ArrayList<String>();
          for ( Bundle bundle : bundleContext.getBundles() ) {
            if ( blueprintStateService.hasBlueprint( bundle.getBundleId() ) ) {
              if ( !blueprintStateService.isBlueprintLoaded( bundle.getBundleId() ) && !blueprintStateService
                  .isBlueprintFailed( bundle.getBundleId() ) ) {
                unloadedBlueprints.add( bundle.getSymbolicName() );
              }
            }
          }
          if ( unloadedBlueprints.size() > 0 ) {
            if ( System.currentTimeMillis() - timeout > entryTime ) {
              IServiceBarrier serviceBarrier = IServiceBarrierManager.LOCATOR.getManager().getServiceBarrier( "KarafFeatureWatcherBarrier" );
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
            continue;
          }
          break;
        }

      } catch ( Exception e ) {
        throw new BlueprintWatcherException( "Unknown error in KarafBlueprintWatcher", e );
      }
    }
  }
}
