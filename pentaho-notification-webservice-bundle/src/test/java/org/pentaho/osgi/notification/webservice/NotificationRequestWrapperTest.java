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
