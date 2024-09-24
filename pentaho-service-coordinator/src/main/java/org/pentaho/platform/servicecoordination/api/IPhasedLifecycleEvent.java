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

/**
 * The interface for events sent by the IPhaseLifecycleManager. It supports access to the lifecycle phase and object
 * associated with that phase.
 * <p/>
 * Listeners receiving this event must call accept for the lifecycle to be considered complete. Failure to do so will
 * block the ability to move the phase in the IPhaseLifecycleManager.
 * <p/>
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleEvent<T> {
  /**
   * Get the turrent Phase of the IPhasedLifeccleManager when this event was created.
   *
   * @return current phase
   */
  int getPhase();

  /**
   * Get the object associated with the current phase.
   *
   * @return notification object
   */
  T getNotificationObject();

  /**
   * Accept the phase notification. Once all listeners have accepted the current phase the manager is free to move to
   * other phases
   */
  void accept();

  /**
   * Calling this method will interrupt the IPhaseLifecycleManager.
   *
   * @param t Related Exception
   */
  void exception( Throwable t );
}
