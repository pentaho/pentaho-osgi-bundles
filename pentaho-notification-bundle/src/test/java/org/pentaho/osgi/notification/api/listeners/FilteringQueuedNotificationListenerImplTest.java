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

package org.pentaho.osgi.notification.api.listeners;

import org.junit.Test;
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.Notifier;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/19/14.
 */
public class FilteringQueuedNotificationListenerImplTest {
  @Test
  public void testNotifyNotMatching() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    NotificationObject notificationObject = mock( NotificationObject.class );
    when( matchCondition.matches( notificationObject ) ).thenReturn( false );
    filteringQueuedNotificationListener.notify( notificationObject );
    verifyNoMoreInteractions( delegate );
  }

  @Test
  public void testNotifytNotMatchingCondition() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    Object object = new Object();
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    NotificationObject notificationObject = mock( NotificationObject.class );
    when( notificationObject.getObject() ).thenReturn( object );
    when( matchCondition.matches( notificationObject ) ).thenReturn( false );
    filteringQueuedNotificationListener.notify( notificationObject );
    verifyNoMoreInteractions( delegate );
  }

  @Test
  public void testNotifyMatching() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    Object object = new Object();
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    NotificationObject notificationObject = mock( NotificationObject.class );
    when( notificationObject.getObject() ).thenReturn( object );
    when( matchCondition.matches( notificationObject ) ).thenReturn( true );
    filteringQueuedNotificationListener.notify( notificationObject );
    verify( delegate ).notify( notificationObject );
  }

  @Test
  public void testRegisterNotMatching() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    Notifier notifier = mock( Notifier.class );
    filteringQueuedNotificationListener.registerWithIfRelevant( notifier );
    verify( notifier, never() ).register( filteringQueuedNotificationListener );
  }

  @Test
  public void testRegisterMatching() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    Notifier notifier = mock( Notifier.class );
    when( notifier.getEmittedTypes() ).thenReturn( new HashSet<String>( Arrays.asList( type ) ) );
    filteringQueuedNotificationListener.registerWithIfRelevant( notifier );
    verify( notifier ).register( filteringQueuedNotificationListener );
  }

  @Test
  public void testUnRegisterMatching() {
    String type = "test-type";
    MatchCondition matchCondition = mock( MatchCondition.class );
    NotificationListener delegate = mock( NotificationListener.class );
    FilteringNotificationListenerImpl filteringQueuedNotificationListener =
      new FilteringNotificationListenerImpl( new HashSet<String>( Arrays.asList( type ) ), matchCondition, delegate );
    Notifier notifier = mock( Notifier.class );
    when( notifier.getEmittedTypes() ).thenReturn( new HashSet<String>( Arrays.asList( type ) ) );
    filteringQueuedNotificationListener.registerWithIfRelevant( notifier );
    filteringQueuedNotificationListener.unregisterWithAll();
    verify( notifier ).register( filteringQueuedNotificationListener );
    verify( notifier ).unregister( filteringQueuedNotificationListener );
  }
}
