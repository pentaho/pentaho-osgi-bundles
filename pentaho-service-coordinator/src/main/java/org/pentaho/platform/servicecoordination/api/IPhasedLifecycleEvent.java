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
