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
package org.pentaho.osgi.notification.api.listeners;

import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bryan on 12/31/14.
 */
public class QueuedNotificationListenerImpl implements NotificationListener {
  private final BlockingQueue<NotificationObject> queuedNotifications = new LinkedBlockingQueue<NotificationObject>();

  @Override public void notify( NotificationObject notificationObject ) {
    boolean success = false;
    while ( !success ) {
      try {
        queuedNotifications.put( notificationObject );
        success = true;
      } catch ( InterruptedException e ) {
        // Ignore
      }
    }
  }

  public BlockingQueue<NotificationObject> getQueuedNotifications() {
    return queuedNotifications;
  }
}
