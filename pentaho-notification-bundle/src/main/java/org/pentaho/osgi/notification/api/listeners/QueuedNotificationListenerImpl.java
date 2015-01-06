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
