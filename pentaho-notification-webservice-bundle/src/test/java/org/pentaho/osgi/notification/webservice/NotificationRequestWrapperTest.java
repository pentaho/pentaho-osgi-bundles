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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 8/22/14.
 */
public class NotificationRequestWrapperTest {
  @Test
  public void testNoArgConstructor() {
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper(  );
    assertNull( notificationRequestWrapper.getRequests() );
  }

  @Test
  public void testRequestsConstructor() {
    NotificationRequest request = mock( NotificationRequest.class );
    List<NotificationRequest> requests = new ArrayList<NotificationRequest>( Arrays.asList(request) );
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper( requests );
    assertEquals( requests, notificationRequestWrapper.getRequests() );
  }

  @Test
  public void testSetRequests() {
    NotificationRequest request = mock( NotificationRequest.class );
    List<NotificationRequest> requests = new ArrayList<NotificationRequest>( Arrays.asList(request) );
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper( );
    notificationRequestWrapper.setRequests( requests );
    assertEquals( requests, notificationRequestWrapper.getRequests() );
  }
}
