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
