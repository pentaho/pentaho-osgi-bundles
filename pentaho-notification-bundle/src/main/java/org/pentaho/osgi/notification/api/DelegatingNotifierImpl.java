/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
