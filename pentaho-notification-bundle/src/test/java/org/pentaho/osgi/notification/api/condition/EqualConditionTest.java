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
