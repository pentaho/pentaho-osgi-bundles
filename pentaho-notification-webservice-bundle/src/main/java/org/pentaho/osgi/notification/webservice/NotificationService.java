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

import org.pentaho.osgi.notification.api.NotificationAggregator;
import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.listeners.FilteringNotificationListenerImpl;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bryan on 8/21/14.
 */
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class NotificationService {
  public static final long TIMEOUT = 30 * 1000;
  private NotificationAggregator notificationAggregator;
  private boolean isLocalExecutor = true;
  private ExecutorService executorService = Executors.newCachedThreadPool();

  public void setNotificationAggregator( NotificationAggregator notificationAggregator ) {
    this.notificationAggregator = notificationAggregator;
  }

  public synchronized void setExecutorService( ExecutorService executorService ) {
    if ( isLocalExecutor ) {
      isLocalExecutor = false;
      this.executorService.shutdown();
    }
    this.executorService = executorService;
  }

  @POST
  @Path( "/" )
  public void getNotifications( @Suspended final AsyncResponse asyncResponse,
                                NotificationRequestWrapper notificationRequestWrapper ) {
    asyncResponse.setTimeout( TIMEOUT, TimeUnit.MILLISECONDS );
    NotificationRequestMatchCondition notificationRequestMatchCondition =
      new NotificationRequestMatchCondition( notificationRequestWrapper );
    final FilteringNotificationListenerImpl filteringNotificationListener = new FilteringNotificationListenerImpl(
      notificationRequestMatchCondition.getTypes(), notificationRequestMatchCondition, null );
    final AtomicBoolean listenerFired = new AtomicBoolean( false );

    asyncResponse.setTimeoutHandler( new TimeoutHandler() {
      @Override public void handleTimeout( AsyncResponse asyncResponse ) {
        resumeIfFirst( asyncResponse, filteringNotificationListener,
          new NotificationResponse( new ArrayList<NotificationObject>(  ) ), listenerFired );
      }
    } );

    filteringNotificationListener.setDelegate( new NotificationListener() {
      @Override public void notify( final NotificationObject notificationObject ) {
        resumeIfFirst( asyncResponse, filteringNotificationListener,
          new NotificationResponse( Arrays.asList( notificationObject ) ), listenerFired );
      }
    } );
    notificationAggregator.registerFilteringListener( filteringNotificationListener );
  }

  private void resumeIfFirst( final AsyncResponse asyncResponse,
                              final FilteringNotificationListenerImpl filteringNotificationListener,
                              final NotificationResponse response, AtomicBoolean listenerFired ) {
    if ( !listenerFired.getAndSet( true ) ) {
      asyncResponse.resume( response );
      executorService.submit( new Runnable() {
        @Override public void run() {
          notificationAggregator.unregisterFilteringListener( filteringNotificationListener );
        }
      } );
    }
  }
}
