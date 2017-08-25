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
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.match.AndCondition;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/22/14.
 */
public class AndConditionTest {
  private MatchCondition delegate1;
  private MatchCondition delegate2;
  private AndCondition andCondition;

  @Before
  public void setup() {
    delegate1 = mock( MatchCondition.class );
    delegate2 = mock( MatchCondition.class );
    andCondition = new AndCondition( Arrays.asList( delegate1, delegate2 ) );
  }

  @Test
  public void testBothTrue() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( true );
    when( delegate2.matches( object ) ).thenReturn( true );
    assertTrue( andCondition.matches( object ) );
  }

  @Test
  public void testOneTrue() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( false );
    when( delegate2.matches( object ) ).thenReturn( true );
    assertFalse( andCondition.matches( object ) );
  }

  @Test
  public void testBothFalse() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( false );
    when( delegate2.matches( object ) ).thenReturn( false );
    assertFalse( andCondition.matches( object ) );
  }
}
