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
