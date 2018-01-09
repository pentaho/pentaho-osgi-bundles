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
