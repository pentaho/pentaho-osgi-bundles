/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.MatchConditionException;
import org.pentaho.osgi.notification.api.NotificationAggregator;
import org.pentaho.osgi.notification.api.NotificationObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/22/14.
 */
public class NotificationServiceTest {
  private NotificationAggregator notificationAggregator;
  private NotificationService service;

  @Before
  public void setup() {
    notificationAggregator = mock( NotificationAggregator.class );
    service = new NotificationService();
    service.setNotificationAggregator( notificationAggregator );
  }

  @Test
  public void testGetNotification() {
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper();
    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setNotificationType( "test-type" );
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry( "test-id", 10L );
    notificationRequest.setEntries( Arrays.asList(notificationRequestEntry) );
    notificationRequestWrapper.setRequests( Arrays.asList( notificationRequest ) );
    final List<NotificationObject> result = new ArrayList<NotificationObject>();
    NotificationObject notificationObject = mock( NotificationObject.class );
    result.add( notificationObject );
    when( notificationAggregator
      .getNotificationsBlocking( anySet(), any( MatchCondition.class ), eq( NotificationService.TIMEOUT ) ) ).thenAnswer( new Answer<Object>() {

      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        List<String> typeList = new ArrayList<String>(
          (java.util.Collection<String>) invocation.getArguments()[0] );
        assertEquals( 1, typeList.size() );
        assertEquals( "test-type", typeList.get( 0 ) );
        NotificationRequestMatchCondition notificationRequestMatchCondition =
          (NotificationRequestMatchCondition) invocation.getArguments()[1];
        assertTrue( notificationRequestMatchCondition.matches( new NotificationObject( "test-type", "test-id", 10L, new Object() ) ) );
        return result;
      }
    } );
    assertEquals( result, service.getNotifications( notificationRequestWrapper ).getNotificationObjects() );
  }
}
