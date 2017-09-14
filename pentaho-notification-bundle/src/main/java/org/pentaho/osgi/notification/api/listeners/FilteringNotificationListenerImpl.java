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

import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.Notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bryan on 12/31/14.
 */
public class FilteringNotificationListenerImpl implements NotificationListener {
  private final Set<String> types;
  private final List<Notifier> notifiersRegisteredWith = new ArrayList<Notifier>();
  private final MatchCondition matchCondition;
  private NotificationListener delegate;

  public FilteringNotificationListenerImpl( Set<String> types, MatchCondition matchCondition,
                                            NotificationListener delegate ) {
    this.types = types;
    this.matchCondition = matchCondition;
    this.delegate = delegate;
  }

  public void setDelegate( NotificationListener delegate ) {
    this.delegate = delegate;
  }

  @Override public void notify( NotificationObject notificationObject ) {
    if ( matchCondition.matches( notificationObject ) ) {
      delegate.notify( notificationObject );
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

  public Set<String> getTypes() {
    return new HashSet<String>( types );
  }

  public MatchCondition getMatchCondition() {
    return matchCondition;
  }
}
