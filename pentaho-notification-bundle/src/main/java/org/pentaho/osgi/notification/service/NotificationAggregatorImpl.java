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

package org.pentaho.osgi.notification.service;

import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.NotificationAggregator;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.Notifier;
import org.pentaho.osgi.notification.api.NotifierWithHistory;
import org.pentaho.osgi.notification.api.listeners.FilteringNotificationListenerImpl;
import org.pentaho.osgi.notification.api.listeners.QueuedNotificationListenerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by bryan on 9/18/14.
 */
public class NotificationAggregatorImpl implements NotificationAggregator {
  private final Set<Notifier> notifiers = new CopyOnWriteArraySet<Notifier>();
  private final Set<FilteringNotificationListenerImpl> notificationListeners =
    new HashSet<FilteringNotificationListenerImpl>();
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  public void addNotifier( NotifierWithHistory notifier ) {
    notifiers.add( notifier );
    readWriteLock.readLock().lock();
    try {
      for ( FilteringNotificationListenerImpl notificationListener : notificationListeners ) {
        notificationListener.registerWithIfRelevant( notifier );
      }
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public void removeNotifier( NotifierWithHistory notifier ) {
    notifiers.remove( notifier );
  }

  public List<Notifier> getEligibleNotifiers( Set<String> types ) {
    List<Notifier> result = new ArrayList<Notifier>();
    for ( Notifier notifier : notifiers ) {
      if ( !Collections.disjoint( notifier.getEmittedTypes(), types ) ) {
        result.add( notifier );
      }
    }
    return result;
  }

  protected List<NotificationObject> getNotifications( List<Notifier> eligibleNotifiers,
                                                       MatchCondition matchCondition ) {
    List<NotificationObject> notifications = new ArrayList<NotificationObject>();
    for ( Notifier notifier : eligibleNotifiers ) {
      if ( notifier instanceof NotifierWithHistory ) {
        for ( NotificationObject previousNotificationObject : ( (NotifierWithHistory) notifier )
          .getPreviousNotificationObjects() ) {
          if ( matchCondition == null || matchCondition.matches( previousNotificationObject ) ) {
            notifications.add( previousNotificationObject );
          }
        }
      }
    }
    return notifications;
  }

  @Override public List<NotificationObject> getNotifications( Set<String> types, MatchCondition matchCondition ) {
    return getNotifications( getEligibleNotifiers( types ), matchCondition );
  }

  @Override public List<NotificationObject> getNotificationsBlocking( Set<String> types, MatchCondition matchCondition,
                                                                      long maxBlockTime ) {
    long endTime = 0;
    if ( maxBlockTime > 0 ) {
      endTime = System.currentTimeMillis() + maxBlockTime;
    }
    if ( endTime <= 0 ) {
      endTime = Long.MAX_VALUE;
    }
    QueuedNotificationListenerImpl queuedNotificationListener = new QueuedNotificationListenerImpl();
    FilteringNotificationListenerImpl filteringNotificationListener =
      new FilteringNotificationListenerImpl( types, matchCondition, queuedNotificationListener );
    try {
      List<NotificationObject> notifications =
        registerFilteringListenerAndReturnPrevious( filteringNotificationListener );
      BlockingQueue<NotificationObject> blockingQueue = queuedNotificationListener.getQueuedNotifications();
      while ( notifications.size() == 0 && System.currentTimeMillis() < endTime ) {
        try {
          NotificationObject notificationObject =
            blockingQueue.poll( endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS );
          // Empty the queue
          while ( notificationObject != null ) {
            notifications.add( notificationObject );
            notificationObject = blockingQueue.poll();
          }
        } catch ( InterruptedException e ) {
          //ignore
        }
      }
      return notifications;
    } finally {
      unregisterFilteringListener( filteringNotificationListener );
    }
  }

  @Override public void registerFilteringListener(
    FilteringNotificationListenerImpl filteringNotificationListener ) {
    for ( NotificationObject notificationObject : registerFilteringListenerAndReturnPrevious(
      filteringNotificationListener ) ) {
      filteringNotificationListener.notify( notificationObject );
    }
  }

  private List<NotificationObject> registerFilteringListenerAndReturnPrevious(
    FilteringNotificationListenerImpl filteringNotificationListener ) {
    readWriteLock.writeLock().lock();
    try {
      notificationListeners.add( filteringNotificationListener );
    } finally {
      readWriteLock.writeLock().unlock();
    }

    List<Notifier> eligibleNotifiers = getEligibleNotifiers( filteringNotificationListener.getTypes() );
    for ( Notifier notifier : eligibleNotifiers ) {
      filteringNotificationListener.registerWithIfRelevant( notifier );
    }
    return getNotifications( eligibleNotifiers, filteringNotificationListener.getMatchCondition() );
  }

  @Override public void unregisterFilteringListener(
    FilteringNotificationListenerImpl filteringNotificationListener ) {
    readWriteLock.writeLock().lock();
    try {
      notificationListeners.remove( filteringNotificationListener );
    } finally {
      readWriteLock.writeLock().unlock();
    }
    filteringNotificationListener.unregisterWithAll();
  }
}
