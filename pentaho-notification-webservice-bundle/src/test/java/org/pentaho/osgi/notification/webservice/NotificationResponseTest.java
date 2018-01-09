/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.notification.webservice;

import org.junit.Test;
import org.pentaho.osgi.notification.api.NotificationObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 8/22/14.
 */
public class NotificationResponseTest {
  @Test
  public void testNoArgConstructor() {
    NotificationResponse notificationResponse = new NotificationResponse();
    assertNull( notificationResponse.getNotificationObjects() );
  }

  @Test
  public void testNotificationTypeChangedItemsConstructor() {
    NotificationObject changedItem = mock( NotificationObject.class );
    List<NotificationObject> changedItems = new ArrayList<NotificationObject>( Arrays.asList( changedItem ) );
    NotificationResponse notificationResponse = new NotificationResponse( changedItems );
    assertEquals( changedItems, notificationResponse.getNotificationObjects() );
  }

  @Test
  public void testSetChangedItems() {
    NotificationObject changedItem = mock( NotificationObject.class );
    List<NotificationObject> changedItems = new ArrayList<NotificationObject>( Arrays.asList( changedItem ) );
    NotificationResponse notificationResponse = new NotificationResponse();
    notificationResponse.setNotificationObjects( changedItems );
    assertEquals( changedItems, notificationResponse.getNotificationObjects() );
  }
}
