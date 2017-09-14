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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by bryan on 8/22/14.
 */
public class NotificationRequestEntryTest {
  @Test
  public void testNoArgConstructor() {
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry(  );
    assertNull( notificationRequestEntry.getId() );
    assertNull( null, notificationRequestEntry.getSequence() );
  }

  @Test
  public void testKeyValueConstructor() {
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry( "TEST", 10L );
    assertEquals( "TEST", notificationRequestEntry.getId() );
    assertEquals( Long.valueOf( 10L ), notificationRequestEntry.getSequence() );
  }

  @Test
  public void testSetKey() {
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry(  );
    notificationRequestEntry.setId( "TEST_KEY" );
    assertEquals( "TEST_KEY", notificationRequestEntry.getId() );
  }

  @Test
  public void testSetValue() {
    NotificationRequestEntry notificationRequestEntry = new NotificationRequestEntry(  );
    notificationRequestEntry.setSequence( 11L );
    assertEquals( Long.valueOf( 11L ), notificationRequestEntry.getSequence() );
  }
}
