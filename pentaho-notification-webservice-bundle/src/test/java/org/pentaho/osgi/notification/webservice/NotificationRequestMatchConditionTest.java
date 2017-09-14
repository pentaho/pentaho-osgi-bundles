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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.notification.api.NotificationObject;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by bryan on 9/22/14.
 */
public class NotificationRequestMatchConditionTest {
  private NotificationRequestWrapper notificationRequestWrapper;
  private NotificationRequest notificationRequest;
  private NotificationRequestEntry notificationRequestEntry;
  private NotificationRequestEntry notificationRequestEntry2;

  @Before
  public void setup() {
    notificationRequestWrapper = new NotificationRequestWrapper();
    notificationRequest = new NotificationRequest();
    notificationRequest.setNotificationType( "test-type" );
    notificationRequestEntry = new NotificationRequestEntry( "test-id", 10L );
    notificationRequestEntry2 = new NotificationRequestEntry( "test-id-2", 11L );
    notificationRequest.setEntries( Arrays.asList( notificationRequestEntry, notificationRequestEntry2 ) );
    notificationRequestWrapper.setRequests( Arrays.asList( notificationRequest ) );
  }

  @Test
  public void testMatch() {
    assertTrue( new NotificationRequestMatchCondition( notificationRequestWrapper ).matches( new NotificationObject( "test-type", "test-id", 10L, new Object() ) ) );
  }

  @Test
  public void testNull() {
    assertFalse( new NotificationRequestMatchCondition( notificationRequestWrapper )
      .matches( null ) );
  }

  @Test
  public void testWrongType() {
    assertFalse( new NotificationRequestMatchCondition( notificationRequestWrapper ).matches( new NotificationObject( "test-type-fake", "test-id", 10L, new Object() ) ) );
  }

  @Test
  public void testWrongFake() {
    assertFalse( new NotificationRequestMatchCondition( notificationRequestWrapper ).matches( new NotificationObject( "test-type", "test-id-fake", 10L, new Object() ) ) );
  }

  @Test
  public void testLowSeq() {
    assertFalse( new NotificationRequestMatchCondition( notificationRequestWrapper ).matches( new NotificationObject( "test-type", "test-id", 9L, new Object() ) ) );
  }

  @Test
  public void testNullSeq() {
    notificationRequestEntry.setSequence( null );
    assertTrue( new NotificationRequestMatchCondition( notificationRequestWrapper ).matches( new NotificationObject( "test-type", "test-id", 9L, new Object() ) ) );
  }
}
