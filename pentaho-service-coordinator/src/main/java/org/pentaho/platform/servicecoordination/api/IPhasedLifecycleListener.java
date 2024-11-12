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

package org.pentaho.platform.servicecoordination.api;

/**
 * A listener for an IPhasedLifecycleManager. It will receive notification whenever the phase of the manager changes.
 * The listener must accept the event given on the phase change for the phase to be considered complete. Listeners
 * should not block on the phase notification if possible. The intention is for this notification and acceptance to be
 * fully Asynchronous. Returning early will allow other listeners to be called.
 * <p/>
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleListener<T> {
  /**
   * Called whenever the IPhasedLifecycleManager this listener is attached to changes phase. The listener must
   * eventually accept the event for the phase to be considered complete.
   *
   * @param event Notification Event for the current Phase
   */
  void onPhaseChange( IPhasedLifecycleEvent<T> event );
}
