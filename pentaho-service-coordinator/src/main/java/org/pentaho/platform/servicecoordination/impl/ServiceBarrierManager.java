package org.pentaho.platform.servicecoordination.impl;

import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;

import java.util.WeakHashMap;

/**
 * Created by nbaker on 2/20/15.
 */
public class ServiceBarrierManager implements IServiceBarrierManager {
  WeakHashMap<String, IServiceBarrier> barriers = new WeakHashMap<String, IServiceBarrier>(  );

  @Override public IServiceBarrier getServiceBarrier( String serviceID ) {
    if(barriers.containsKey( serviceID ) ){
      return barriers.get( serviceID );
    }
    PhaserServiceBarrier phaserServiceBarrier = new PhaserServiceBarrier( serviceID );
    barriers.put( serviceID, phaserServiceBarrier ) ;
    return phaserServiceBarrier;
  }
}
