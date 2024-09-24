/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.osgi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.karaf.bundle.core.BundleState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.pentaho.osgi.api.BlueprintStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bmorrise on 9/3/15.
 */
public class BlueprintStateServiceImpl implements BlueprintStateService, BlueprintListener {
  private static final Logger LOG = LoggerFactory.getLogger( BlueprintStateServiceImpl.class );
  private final Map<Long, BlueprintEvent> states;
  private final List<Long> bluePrints;
  private final BundleContext bundleContext;

  public BlueprintStateServiceImpl( BundleContext bundleContext ) {
    states = new ConcurrentHashMap<Long, BlueprintEvent>();
    bluePrints = new ArrayList<Long>();
    this.bundleContext = bundleContext;
  }

  @Override public Boolean isBlueprintLoaded( long bundleId ) {
    return hasBlueprint( bundleId ) && getBundleState( bundleId ) == BundleState.Active;
  }

  @Override public Boolean isBlueprintFailed( long bundleId ) {
    return hasBlueprint( bundleId ) && getBundleState( bundleId ) == BundleState.Failure;
  }

  @Override public Boolean isBlueprintTryingToLoad( long bundleId ) {
    if ( !hasBlueprint( bundleId ) ) {
      return false;
    }
    switch ( getBundleState( bundleId ) ) {
      case GracePeriod:
      case Waiting:
      case Starting:
        return true;
    }
    return false;
  }

  @Override public Boolean hasBlueprint( long bundleId ) {
    if ( bluePrints.size() == 0 ) {
      for ( Bundle bundle : bundleContext.getBundles() ) {
        if ( bundle.getBundleId() == 0 ) {
          continue;
        }
        if ( bundle.getResource( "OSGI-INF/blueprint" ) != null ) {
          bluePrints.add( bundle.getBundleId() );
        }
      }
    }

    return bluePrints.contains( bundleId );
  }

  @Override public void blueprintEvent( BlueprintEvent blueprintEvent ) {
    if ( LOG.isDebugEnabled() ) {
      BundleState state = getBundleState( blueprintEvent.getBundle().getBundleId() );
      LOG.debug(
          "Blueprint app state changed to " + state + " for bundle " + blueprintEvent.getBundle().getBundleId() );
    }
    states.put( blueprintEvent.getBundle().getBundleId(), blueprintEvent );
  }

  @Override public BundleState getBundleState( long bundleId ) {
    BlueprintEvent blueprintEvent = states.get( bundleId );

    if ( blueprintEvent == null ) {
      return BundleState.Unknown;
    }
    switch ( blueprintEvent.getType() ) {
      case BlueprintEvent.CREATING:
        return BundleState.Starting;
      case BlueprintEvent.CREATED:
        return BundleState.Active;
      case BlueprintEvent.DESTROYING:
        return BundleState.Stopping;
      case BlueprintEvent.DESTROYED:
        return BundleState.Resolved;
      case BlueprintEvent.FAILURE:
        return BundleState.Failure;
      case BlueprintEvent.GRACE_PERIOD:
        return BundleState.GracePeriod;
      case BlueprintEvent.WAITING:
        return BundleState.Waiting;
      default:
        return BundleState.Unknown;
    }
  }

  @Override public String[] getBundleMissDependencies( long bundleId ) {
    BlueprintEvent blueprintEvent = states.get( bundleId );
    if ( blueprintEvent == null ) {
      return null;
    }
    return blueprintEvent.getDependencies();
  }

  @Override public Throwable getBundleFailureCause( long bundleId ) {
    BlueprintEvent blueprintEvent = states.get( bundleId );
    if ( blueprintEvent == null ) {
      return null;
    }
    return blueprintEvent.getCause();
  }
}
