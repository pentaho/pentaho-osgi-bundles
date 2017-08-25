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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

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
