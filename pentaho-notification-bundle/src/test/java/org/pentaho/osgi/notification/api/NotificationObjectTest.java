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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 9/19/14.
 */
public class NotificationObjectTest {
  String type;
  String id;
  long sequence;
  Object object;
  NotificationObject notificationObject;

  @Before
  public void setup() {
    type = "test-type";
    id = "test-id";
    sequence = 99L;
    object = new Object();
    notificationObject = new NotificationObject( type, id, sequence, object );
  }

  @Test
  public void testIdSequenceObjectConstructor() {
    assertEquals( type, notificationObject.getType() );
    assertEquals( id, notificationObject.getId() );
    assertEquals( sequence, notificationObject.getSequence() );
    assertEquals( object, notificationObject.getObject() );
  }
}
