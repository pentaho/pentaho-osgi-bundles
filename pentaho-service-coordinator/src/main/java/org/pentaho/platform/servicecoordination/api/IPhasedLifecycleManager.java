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

import java.util.concurrent.Executor;

/**
 * <p> A Lifecycle Manager which notifies listeners using a configurable executor. Implementations of this manager are
 * built around the concept of phases (an integer) which the listeners are notified of. All listeners must accept the
 * phase event before the manager can change phases again. </p> <p> Calls to change the phase before all listeners have
 * accepted the previous phase change will block. </p> <p> By supplying an Executor listeners can be notified
 * asynchronously if desired. </p>
 * <p/>
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleManager<T> {
  /**
   * Get the current phase of the manager
   *
   * @return
   */
  int getPhase();

  /**
   * Adds a listener to the manager. The added listener will be notified on the next phase change. It will not be caught
   * up with the existing phase
   *
   * @param listener
   */
  void addLifecycleListener( IPhasedLifecycleListener<T> listener );

  /**
   * Removes a listener.
   *
   * @param listener
   */
  void removeLifecycleListener( IPhasedLifecycleListener<T> listener );

  /**
   * Get the number of listeners
   *
   * @return
   */
  int getListenerCount();

  /**
   * Advance a phase and wait for all listeners to accept the event.
   *
   * @return
   * @throws InterruptedException
   */
  int advanceAndWait() throws InterruptedException;

  /**
   * Retreat the phase and wait for all listeners to accept.
   *
   * @return
   * @throws InterruptedException
   */
  int retreatAndWait() throws InterruptedException;

  /**
   * Set the phase to a specified point, notifies listeners and waits for them to accept.
   *
   * @param phase
   * @throws InterruptedException
   */
  void setPhaseAndWait( int phase ) throws InterruptedException;

  /**
   * Advance the phase and notify listeners. Call returns immediately but blocks if the last phase has not been accepted
   * by all listeners. As soon as the phase change becomes available the call will return.
   *
   * @return
   * @throws InterruptedException
   */
  int advance() throws InterruptedException;

  /**
   * Retreat the phase and notify listeners. Call returns immediately but blocks if the last phase has not been accepted
   * by all listeners. As soon as the phase change becomes available the call will return.
   *
   * @return
   * @throws InterruptedException
   */
  int retreat() throws InterruptedException;

  /**
   * Set the phase to the specified point. Call returns immediately but blocks if the last phase has not been accepted
   * by all listeners. As soon as the phase change becomes available the call will return.
   *
   * @param phase
   */
  void setPhase( int phase );

  /**
   * Supply an Executor to handle the notification of the listeners.
   *
   * @param executorService
   */
  void setExecutor( Executor executorService );

  /**
   * Terminate the manager, currently blocked threads will be interrupted.
   */
  void terminate();

  /**
   * Returns whether or not the manager has been terminated.
   *
   * @return
   */
  boolean isTerminated();
}
