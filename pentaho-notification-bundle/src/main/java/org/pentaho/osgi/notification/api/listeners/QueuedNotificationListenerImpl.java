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
