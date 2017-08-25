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
 * A basic barrier class. It can be used to allow others to block execution until certain conditions are meet.
 * <p/>
 * Barriers allow for extensible coordination between units of code unaware of the other. The coupling is to the barrier
 * instead of each other.
 * <p/>
 * <p/>
 * Created by nbaker on 1/27/15.
 */
public interface IServiceBarrier {
  /**
   * Add a hold on the barrier. The barrier will become unavailable until the hold is released.
   *
   * @return
   */
  int hold();

  /**
   * Release a hold on the barrier. If this was the final hold threads waiting for availability will unblock.
   *
   * @return
   */
  int release();

  /**
   * Return the number of holds on the barrier
   *
   * @return
   */
  int getHoldCount();

  /**
   * Check the availability of the barrier
   *
   * @return
   */
  boolean isAvailable();

  /**
   * Wait for the barrier to become available. Returns immediately if the barrier is available.
   *
   * @return
   * @throws InterruptedException
   */
  int awaitAvailability() throws InterruptedException;

  /**
   * Terminate the barrier. Any blocked threads will be interrupted.
   */
  void terminate();

  /**
   * Check if the barrier has been terminated.
   *
   * @return
   */
  boolean isTerminated();
}
