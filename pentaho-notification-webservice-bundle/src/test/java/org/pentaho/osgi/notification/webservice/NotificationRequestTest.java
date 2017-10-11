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

package org.pentaho.osgi.notification.webservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 8/22/14.
 */
public class NotificationRequestTest {
  @Test
  public void testNoArgConstructor() {
    NotificationRequest notificationRequest = new NotificationRequest(  );
    assertNull( notificationRequest.getEntries() );
    assertNull( notificationRequest.getNotificationType() );
  }

  @Test
  public void testNotificationTypeEntriesConstructor() {
    NotificationRequestEntry entry = mock( NotificationRequestEntry.class );
    List<NotificationRequestEntry> list = new ArrayList<NotificationRequestEntry>( Arrays.asList(entry) );
    NotificationRequest notificationRequest = new NotificationRequest( "TEST_TYPE", list );
    assertEquals( "TEST_TYPE", notificationRequest.getNotificationType() );
    assertEquals( list, notificationRequest.getEntries() );
  }

  @Test
  public void testSetNotificationType() {
    NotificationRequest notificationRequest = new NotificationRequest(  );
    notificationRequest.setNotificationType( "TEST_TYPE" );
    assertEquals( "TEST_TYPE", notificationRequest.getNotificationType() );
  }

  @Test
  public void testSetEntries() {
    NotificationRequestEntry entry = mock( NotificationRequestEntry.class );
    List<NotificationRequestEntry> list = new ArrayList<NotificationRequestEntry>( Arrays.asList(entry) );
    NotificationRequest notificationRequest = new NotificationRequest( );
    notificationRequest.setEntries( list );
    assertEquals( list, notificationRequest.getEntries() );
  }
}
