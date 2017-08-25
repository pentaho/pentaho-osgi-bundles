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

package org.pentaho.osgi.notification.api;

/**
 * Created by bryan on 9/18/14.
 */
public class NotificationObject {
  private final String type;
  private final String id;
  private final long sequence;
  private final Object object;

  public NotificationObject( String type, String id, long sequence, Object object ) {
    this.type = type;
    this.id = id;
    this.sequence = sequence;
    this.object = object;

  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public Object getObject() {
    return object;
  }

  public long getSequence() {
    return sequence;
  }
}
