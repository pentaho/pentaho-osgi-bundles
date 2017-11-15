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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.osgi.notification.api.NotificationAggregator;
import org.pentaho.osgi.notification.api.NotificationListener;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.listeners.FilteringNotificationListenerImpl;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
  private final Map<String, NotificationTypeObjectMapper> mapperConcurrentHashMap =
    new ConcurrentHashMap<String, NotificationTypeObjectMapper>();
  private NotificationAggregator notificationAggregator;
  private boolean isLocalExecutor = true;
  private static ExecutorService executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
    @Override
    public Thread newThread( Runnable r ) {
      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "NotificationService pool" );
      return thread;
    }
  } );

  public void setNotificationAggregator( NotificationAggregator notificationAggregator ) {
    this.notificationAggregator = notificationAggregator;
  }

  public void addNotificationTypeObjectMapper( NotificationTypeObjectMapper mapper, Map properties ) {
    if ( mapper != null ) {
      mapperConcurrentHashMap.put( mapper.getType(), mapper );
    }
  }

  public void removeNotificationTypeObjectMapper( NotificationTypeObjectMapper mapper, Map properties ) {
    if ( mapper != null ) {
      mapperConcurrentHashMap.remove( mapper.getType() );
    }
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
          new NotificationResponse( new ArrayList<NotificationObject>() ), listenerFired );
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
      asyncResponse.resume( new StreamingOutput() {
        @Override public void write( OutputStream output ) throws IOException, WebApplicationException {
          ObjectMapper objectMapper = new ObjectMapper();
          JsonFactory jsonFactory = objectMapper.getJsonFactory();
          JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator( output );
          jsonGenerator.writeStartObject();
          jsonGenerator.writeFieldName( "notificationObjects" );
          jsonGenerator.writeStartArray();
          for ( NotificationObject notificationObject : response.getNotificationObjects() ) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField( "id", notificationObject.getId() );
            String type = notificationObject.getType();
            jsonGenerator.writeStringField( "type", type );
            jsonGenerator.writeNumberField( "sequence", notificationObject.getSequence() );
            jsonGenerator.writeFieldName( "object" );
            NotificationTypeObjectMapper notificationTypeObjectMapper = mapperConcurrentHashMap.get( type );
            ObjectMapper notificationObjectMapper = objectMapper;
            if ( notificationTypeObjectMapper != null ) {
              notificationObjectMapper = notificationTypeObjectMapper.getObjectMapper();
            }
            notificationObjectMapper.writeValue( jsonGenerator, notificationObject.getObject() );
            jsonGenerator.writeEndObject();
          }
          jsonGenerator.writeEndArray();
          jsonGenerator.writeEndObject();
          jsonGenerator.flush();
        }
      } );
      executorService.submit( new Runnable() {
        @Override public void run() {
          notificationAggregator.unregisterFilteringListener( filteringNotificationListener );
        }
      } );
    }
  }
}
