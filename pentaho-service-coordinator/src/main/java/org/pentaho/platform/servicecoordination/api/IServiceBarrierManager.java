package org.pentaho.platform.servicecoordination.api;

/**
 * Supplies IServiceBarrier instances by service id.
 *
 * Created by nbaker on 1/26/15.
 */
public interface IServiceBarrierManager {

  /**
   * Get the barrier for the given service ID
   *
   * @param serviceID
   * @return
   */
  IServiceBarrier getServiceBarrier( String serviceID );

}
