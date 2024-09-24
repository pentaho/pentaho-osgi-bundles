/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
