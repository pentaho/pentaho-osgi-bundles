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
