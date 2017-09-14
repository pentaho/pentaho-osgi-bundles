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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.notification.api.NotificationAggregator;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.listeners.FilteringNotificationListenerImpl;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
  public void testGetNotification() throws IOException {
    AsyncResponse asyncResponse = mock( AsyncResponse.class );
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper();
    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setNotificationType( "test-type" );
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry( "test-id", 10L );
    notificationRequest.setEntries( Arrays.asList( notificationRequestEntry ) );
    notificationRequestWrapper.setRequests( Arrays.asList( notificationRequest ) );
    final NotificationObject notificationObject = new NotificationObject( "test-type", "test-id", 11L, null );
    doAnswer( new Answer<Void>() {
      @Override public Void answer( InvocationOnMock invocation ) throws Throwable {
        ( (FilteringNotificationListenerImpl) invocation.getArguments()[ 0 ] ).notify( notificationObject );
        return null;
      }
    } ).when( notificationAggregator ).registerFilteringListener( any( FilteringNotificationListenerImpl.class ) );
    ArgumentCaptor<StreamingOutput> streamingOutputArgumentCaptor = ArgumentCaptor.forClass( StreamingOutput.class );
    service.getNotifications( asyncResponse, notificationRequestWrapper );
    verify( asyncResponse ).resume( streamingOutputArgumentCaptor.capture() );
    StreamingOutput streamingOutput = streamingOutputArgumentCaptor.getValue();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    streamingOutput.write( byteArrayOutputStream );
    NotificationResponse notificationResponse =
      new NotificationResponse( new ArrayList<NotificationObject>( Arrays.asList( notificationObject ) ) );
    ObjectMapper objectMapper = new ObjectMapper();
    assertEquals( objectMapper.readTree( objectMapper.writeValueAsBytes( notificationResponse ) ),
      objectMapper.readTree(
        byteArrayOutputStream.toByteArray() ) );
  }

  @Test
  public void testGetNotificationTimeout() {
    final AsyncResponse asyncResponse = mock( AsyncResponse.class );
    NotificationRequestWrapper notificationRequestWrapper = new NotificationRequestWrapper();
    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setNotificationType( "test-type" );
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry( "test-id", 10L );
    notificationRequest.setEntries( Arrays.asList( notificationRequestEntry ) );
    notificationRequestWrapper.setRequests( Arrays.asList( notificationRequest ) );
    doAnswer( new Answer<Void>() {
      @Override public Void answer( InvocationOnMock invocation ) throws Throwable {
        ( (TimeoutHandler) invocation.getArguments()[ 0 ] ).handleTimeout( asyncResponse );
        ( (TimeoutHandler) invocation.getArguments()[ 0 ] ).handleTimeout( asyncResponse );
        return null;
      }
    } ).when( asyncResponse ).setTimeoutHandler( any( TimeoutHandler.class ) );
    service.getNotifications( asyncResponse, notificationRequestWrapper );
    verify( asyncResponse, times( 1 ) ).resume( any( NotificationResponse.class ) );
  }
}
