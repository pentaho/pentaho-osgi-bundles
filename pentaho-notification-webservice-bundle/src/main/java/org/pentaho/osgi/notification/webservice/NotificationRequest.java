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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by bryan on 8/21/14.
 */
@XmlRootElement
public class NotificationRequest {
  private String notificationType;
  private List<NotificationRequestEntry> entries;

  public NotificationRequest() {
    this( null, null );
  }

  public NotificationRequest( String notificationType,
                              List<NotificationRequestEntry> entries ) {

    this.notificationType = notificationType;
    this.entries = entries;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType( String notificationType ) {
    this.notificationType = notificationType;
  }

  public List<NotificationRequestEntry> getEntries() {
    return entries;
  }

  public void setEntries( List<NotificationRequestEntry> entries ) {
    this.entries = entries;
  }
}
