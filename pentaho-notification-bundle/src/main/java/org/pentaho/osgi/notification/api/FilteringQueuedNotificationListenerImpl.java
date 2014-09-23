/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.notification.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bryan on 9/18/14.
 */
public class FilteringQueuedNotificationListenerImpl implements NotificationListener {
  private final Set<String> types;
  private final BlockingQueue<NotificationObject> queuedNotifications = new LinkedBlockingQueue<NotificationObject>();
  private final List<Notifier> notifiersRegisteredWith = new ArrayList<Notifier>();
  private final MatchCondition matchCondition;

  public FilteringQueuedNotificationListenerImpl( Set<String> types, MatchCondition matchCondition ) {
    this.types = types;
    this.matchCondition = matchCondition;
  }

  @Override public void notify( NotificationObject notificationObject ) {
    if ( matchCondition.matches( notificationObject ) ) {
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
  }

  public synchronized void registerWithIfRelevant( Notifier notifier ) {
    if ( !Collections.disjoint( notifier.getEmittedTypes(), types ) ) {
      notifier.register( this );
      notifiersRegisteredWith.add( notifier );
    }
  }

  public synchronized void unregisterWithAll() {
    for ( Notifier notifier : notifiersRegisteredWith ) {
      notifier.unregister( this );
    }
  }

  public BlockingQueue<NotificationObject> getQueuedNotifications() {
    return queuedNotifications;
  }
}
