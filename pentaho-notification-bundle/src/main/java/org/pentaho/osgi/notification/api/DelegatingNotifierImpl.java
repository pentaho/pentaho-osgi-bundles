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

package org.pentaho.osgi.notification.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bryan on 9/18/14.
 */
public class DelegatingNotifierImpl implements Notifier, NotificationListener, NotifierWithHistory {
  private final Set<String> types;
  private final HasNotificationHistory hasNotificationHistory;
  private final Set<NotificationListener> listeners;

  public DelegatingNotifierImpl( Set<String> types ) {
    this( types, null );
  }

  public DelegatingNotifierImpl( Set<String> types, HasNotificationHistory hasNotificationHistory ) {
    this.types = types;
    this.hasNotificationHistory = hasNotificationHistory;
    this.listeners = Collections.newSetFromMap( new ConcurrentHashMap<NotificationListener, Boolean>() );
  }

  @Override public Set<String> getEmittedTypes() {
    return types;
  }

  @Override public void register( NotificationListener notificationListener ) {
    listeners.add( notificationListener );
    for ( NotificationObject notificationObject : getPreviousNotificationObjects() ) {
      notificationListener.notify( notificationObject );
    }
  }

  @Override public void unregister( NotificationListener notificationListener ) {
    listeners.remove( notificationListener );
  }

  @Override public void notify( NotificationObject notificationObject ) {
    for ( NotificationListener listener : listeners ) {
      listener.notify( notificationObject );
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override public List<NotificationObject> getPreviousNotificationObjects() {
    if ( hasNotificationHistory != null ) {
      return hasNotificationHistory.getPreviousNotificationObjects();
    }
    return Collections.EMPTY_LIST;
  }
}
