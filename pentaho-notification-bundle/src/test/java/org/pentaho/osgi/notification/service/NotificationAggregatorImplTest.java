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
