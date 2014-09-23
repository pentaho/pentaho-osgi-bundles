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

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by bryan on 8/21/14.
 */
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class NotificationService {
  public static final long TIMEOUT = 30 * 1000;
  private NotificationAggregator notificationAggregator;

  public void setNotificationAggregator( NotificationAggregator notificationAggregator ) {
    this.notificationAggregator = notificationAggregator;
  }

  @POST
  @Path( "/" )
  public NotificationResponse getNotifications( NotificationRequestWrapper notificationRequestWrapper ) {
    NotificationRequestMatchCondition notificationRequestMatchCondition =
      new NotificationRequestMatchCondition( notificationRequestWrapper );
    return new NotificationResponse( notificationAggregator
      .getNotificationsBlocking( notificationRequestMatchCondition.getTypes(), notificationRequestMatchCondition,
        TIMEOUT ) );
  }
}
