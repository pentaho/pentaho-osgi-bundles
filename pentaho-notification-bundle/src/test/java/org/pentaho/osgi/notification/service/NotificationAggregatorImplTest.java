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

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.notification.api.NotifierWithHistory;
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/19/14.
 */
public class NotificationAggregatorImplTest {
  @Test
  public void testNonBlockingReturnsEmptyListWithNoNotifiers() {
    NotificationAggregatorImpl notificationAggregator = new NotificationAggregatorImpl();
    assertEquals( 0, notificationAggregator.getNotifications( (Set<String>) null, null ).size() );
  }

  @Test
  public void testNonBlockingReturnsMatchingNotification() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );

    NotificationAggregatorImpl notificationAggregator = new NotificationAggregatorImpl();
    NotifierWithHistory notifier = mock( NotifierWithHistory.class );
    NotifierWithHistory badNotifier = mock( NotifierWithHistory.class );
    NotifierWithHistory notifierWithNonMatchingNotification = mock( NotifierWithHistory.class );
    when( notifier.getEmittedTypes() ).thenReturn( new HashSet<String>( Arrays.asList( type ) ) );
    when( notifierWithNonMatchingNotification.getEmittedTypes() )
      .thenReturn( new HashSet<String>( Arrays.asList( type ) ) );


    NotificationObject notificationObject = mock( NotificationObject.class );
    Object object = new Object();
    when( matchCondition.matches( notificationObject ) ).thenReturn( true );
    NotificationObject nonMatchingNotificationObject = mock( NotificationObject.class );
    when( notifier.getPreviousNotificationObjects() ).thenReturn( Arrays.asList( notificationObject ) );
    when( notifierWithNonMatchingNotification.getPreviousNotificationObjects() )
      .thenReturn( Arrays.asList( nonMatchingNotificationObject ) );
    when( notificationObject.getObject() ).thenReturn( object );
    notificationAggregator.addNotifier( notifier );
    notificationAggregator.addNotifier( badNotifier );
    notificationAggregator.addNotifier( notifierWithNonMatchingNotification );

    List<NotificationObject> notifications =
      notificationAggregator.getNotifications( new HashSet<String>( Arrays.asList( type ) ), matchCondition );
    assertEquals( 1, notifications.size() );
    assertEquals( notificationObject, notifications.get( 0 ) );
  }

  @Test(timeout = 1000L)
  public void testBlockingReturnsAfterTimeout() {
    NotificationAggregatorImpl notificationAggregator = new NotificationAggregatorImpl();
    assertEquals( 0, notificationAggregator.getNotificationsBlocking( new HashSet<String>(  ), null, 500L ).size() );
  }

  @Test
  public void testBlockingReturnsAfterNotification() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    Object object = new Object();

    final NotificationObject notificationObject = mock( NotificationObject.class );
    when( notificationObject.getObject() ).thenReturn( object );
    when( matchCondition.matches( notificationObject ) ).thenReturn( true );
    NotifierWithHistory notifier = mock( NotifierWithHistory.class );
    when( notifier.getEmittedTypes() ).thenReturn( new HashSet<String>( Arrays.asList( type ) ) );
    NotificationAggregatorImpl notificationAggregator = new NotificationAggregatorImpl();
    notificationAggregator.addNotifier( notifier );
    doAnswer( new Answer<Object>() {
      @Override public Object answer( final InvocationOnMock invocation ) throws Throwable {
        new Thread( new Runnable() {
          @Override public void run() {
            try {
              Thread.sleep( 200L );
            } catch ( InterruptedException e ) {
              //Ignore
            }
            ( (NotificationListener) invocation.getArguments()[ 0 ] ).notify( notificationObject );
          }
        } ).start();
        return null;
      }
    } ).when( notifier ).register( any( NotificationListener.class ) );
    List<NotificationObject> notifications =
      notificationAggregator
        .getNotificationsBlocking( new HashSet<String>( Arrays.asList( type ) ), matchCondition, 20000L );
    assertEquals( 1, notifications.size() );
    assertEquals( notificationObject, notifications.get( 0 ) );
  }
}
