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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/19/14.
 */
public class DelegatingNotifierImplTest {
  @Test
  public void testTypesConstructor() {
    DelegatingNotifierImpl delegatingNotifier = new DelegatingNotifierImpl( new HashSet<String>() );
    NotificationListener notificationListener = mock( NotificationListener.class );
    delegatingNotifier.register( notificationListener );
    verifyNoMoreInteractions( notificationListener );
    NotificationObject notificationObject = mock( NotificationObject.class );
    delegatingNotifier.notify( notificationObject );
    verify( notificationListener ).notify( notificationObject );
  }

  @Test
  public void testHasPreviousNotificationObjectsConstructor() {
    NotifierWithHistory notifierWithHistory = mock( NotifierWithHistory.class );
    List<NotificationObject> previousNotificationObjects = new ArrayList<NotificationObject>();
    NotificationObject previousNotificationObject = mock( NotificationObject.class );
    previousNotificationObjects.add( previousNotificationObject );
    when( notifierWithHistory.getPreviousNotificationObjects() ).thenReturn( previousNotificationObjects );
    DelegatingNotifierImpl delegatingNotifier =
      new DelegatingNotifierImpl( new HashSet<String>(), notifierWithHistory );
    NotificationListener notificationListener = mock( NotificationListener.class );

    delegatingNotifier.register( notificationListener );
    verify( notificationListener ).notify( previousNotificationObject );
    verifyNoMoreInteractions( notificationListener );

    NotificationObject notificationObject = mock( NotificationObject.class );
    delegatingNotifier.notify( notificationObject );
    verify( notificationListener ).notify( notificationObject );
  }

  @Test
  public void testUnregister() {
    DelegatingNotifierImpl delegatingNotifier = new DelegatingNotifierImpl( new HashSet<String>() );
    NotificationListener notificationListener = mock( NotificationListener.class );
    delegatingNotifier.register( notificationListener );
    delegatingNotifier.unregister( notificationListener );
    NotificationObject notificationObject = mock( NotificationObject.class );
    delegatingNotifier.notify( notificationObject );
    verifyNoMoreInteractions( notificationListener );
  }

  @Test
  public void testGetEmitted() {
    String type = "test-type";
    Set<String> types = new HashSet<String>( Arrays.asList( type ) );
    NotifierWithHistory notifierWithHistory = mock( NotifierWithHistory.class );
    DelegatingNotifierImpl delegatingNotifier =
      new DelegatingNotifierImpl( new HashSet<String>( Arrays.asList( type ) ), notifierWithHistory );
    assertEquals( types, delegatingNotifier.getEmittedTypes() );
  }
}
