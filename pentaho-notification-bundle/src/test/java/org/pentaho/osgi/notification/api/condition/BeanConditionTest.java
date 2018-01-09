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
package org.pentaho.osgi.notification.api.condition;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.notification.api.NotifierWithHistory;
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.MatchConditionException;
import org.pentaho.osgi.notification.api.NotificationObject;
import org.pentaho.osgi.notification.api.match.BeanPropertyCondition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/22/14.
 */
public class BeanConditionTest {
  private String type;
  private String id;
  private long sequence;
  private Object object;
  private NotificationObject notificationObject;
  private MatchCondition delegate;

  @Before
  public void setup() {
    type = "test-type";
    id = "test-id";
    sequence = 50L;
    object = new Object();
    notificationObject = new NotificationObject( type, id, sequence, object );
    delegate = mock( MatchCondition.class );
  }

  @Test
  public void testWrongClass() throws MatchConditionException {
    assertFalse( new BeanPropertyCondition( NotifierWithHistory.class, "previousNotificationObjects", delegate ).matches( notificationObject ) );
  }

  @Test
  public void testTrue() throws MatchConditionException {
    when( delegate.matches( object ) ).thenReturn( true );
    assertTrue(
      new BeanPropertyCondition( NotificationObject.class, "object", delegate ).matches( notificationObject ) );
  }

  @Test( expected = MatchConditionException.class )
  public void testInvalidProperty() throws MatchConditionException {
    new BeanPropertyCondition( NotifierWithHistory.class, "invalidGetter", delegate );
  }
}
