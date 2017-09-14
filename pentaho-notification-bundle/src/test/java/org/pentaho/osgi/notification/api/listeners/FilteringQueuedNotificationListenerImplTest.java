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
