/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.servicecoordination.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;

/**
 * Created by nbaker on 2/20/15.
 */
public class ServiceBarrierManager implements IServiceBarrierManager {
  WeakHashMap<String, IServiceBarrier> barriers = new WeakHashMap<String, IServiceBarrier>();

  @Override
  public IServiceBarrier getServiceBarrier( String serviceID ) {
    if ( barriers.containsKey( serviceID ) ) {
      return barriers.get( serviceID );
    }
    PhaserServiceBarrier phaserServiceBarrier = new PhaserServiceBarrier( serviceID );
    barriers.put( serviceID, phaserServiceBarrier );
    return phaserServiceBarrier;
  }

  @Override
  public List<IServiceBarrier> getAllServiceBarriers() {
    return new ArrayList<IServiceBarrier>( barriers.values() );
  }

}
