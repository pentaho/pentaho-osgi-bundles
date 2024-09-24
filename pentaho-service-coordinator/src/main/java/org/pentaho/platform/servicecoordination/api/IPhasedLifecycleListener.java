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
