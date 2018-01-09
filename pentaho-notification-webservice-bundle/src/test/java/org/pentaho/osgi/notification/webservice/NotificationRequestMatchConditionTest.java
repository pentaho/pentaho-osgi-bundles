/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
