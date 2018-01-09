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
