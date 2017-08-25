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
