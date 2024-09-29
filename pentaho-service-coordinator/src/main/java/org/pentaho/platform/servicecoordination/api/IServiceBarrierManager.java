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

package org.pentaho.platform.servicecoordination.api;

import java.util.List;

import org.pentaho.platform.servicecoordination.impl.ServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies IServiceBarrier instances by service id.
 *
 * Created by nbaker on 1/26/15.
 */
public interface IServiceBarrierManager {

  /**
   * Get the barrier for the given service ID.  The default implementation can be overridden by setting
   * the org.pentaho.platform.api.engine.IServiceBarrierManager.class system property to the implementing
   * class name.
   *
   * @param serviceID
   * @return
   */
  IServiceBarrier getServiceBarrier( String serviceID );

  List<IServiceBarrier> getAllServiceBarriers();

  public Locator LOCATOR = new Locator();

  class Locator {
    private static final String MANAGER_CLASS = "org.pentaho.platform.api.engine.IServiceBarrierManager.class";
    public IServiceBarrierManager instance;
    private static Logger logger = LoggerFactory.getLogger( IServiceBarrierManager.Locator.class );

    /**
     * The default implementation can be overridden by setting the
     * org.pentaho.platform.api.engine.IServiceBarrierManager.class system property to the implementing class name.
     * @return
     */
    public IServiceBarrierManager getManager() {

      if ( instance == null ) {
        if ( System.getProperty( MANAGER_CLASS ) != null ) {
          try {
            instance = (IServiceBarrierManager) Class.forName( System.getProperty( MANAGER_CLASS ) ).newInstance();
          } catch ( ClassNotFoundException e ) {
            logger.error( "IServiceBarrierManager class not found", e );
          } catch ( InstantiationException | IllegalAccessException e ) {
            logger.error( "IServiceBarrierManager class could not be instantiated", e );
          }
        }
        if ( instance == null ) {
          instance = new ServiceBarrierManager();
        }
      }
      return instance;
    }
  }
}
