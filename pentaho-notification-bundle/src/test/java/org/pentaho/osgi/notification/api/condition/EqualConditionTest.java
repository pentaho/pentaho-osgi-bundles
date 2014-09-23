/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.notification.api.condition;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.notification.api.match.EqualCondition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/22/14.
 */
public class EqualConditionTest {
  @Test
  public void testTrue() {
    final Object test = new Object();
    Object predicate = new Object() {
      @Override public boolean equals( Object obj ) {
        return obj == test;
      }
    };
    assertTrue( new EqualCondition( predicate ).matches( test ) );
  }

  @Test
  public void testFalse() {
    final Object test = new Object();
    Object predicate = new Object() {
      @Override public boolean equals( Object obj ) {
        return obj != test;
      }
    };
    assertFalse( new EqualCondition( predicate ).matches( test ) );
  }

  @Test
  public void testBothNull() {
    Object test = null;
    Object predicate = null;
    assertTrue( new EqualCondition( predicate ).matches( test ) );
  }

  @Test
  public void testTestNull() {
    Object test = null;
    Object predicate = new Object();
    assertFalse( new EqualCondition( predicate ).matches( test ) );
  }

  @Test
  public void testPredicateNull() {
    Object test = new Object();
    Object predicate = null;
    assertFalse( new EqualCondition( predicate ).matches( test ) );
  }
}
