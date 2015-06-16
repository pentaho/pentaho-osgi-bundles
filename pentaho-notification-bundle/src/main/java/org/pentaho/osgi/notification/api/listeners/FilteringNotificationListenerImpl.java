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
