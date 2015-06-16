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
import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.match.OrCondition;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/22/14.
 */
public class OrConditionTest {
  private MatchCondition delegate1;
  private MatchCondition delegate2;
  private OrCondition orCondition;

  @Before
  public void setup() {
    delegate1 = mock( MatchCondition.class );
    delegate2 = mock( MatchCondition.class );
    orCondition = new OrCondition( Arrays.asList( delegate1, delegate2 ) );
  }

  @Test
  public void testBothTrue() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( true );
    when( delegate2.matches( object ) ).thenReturn( true );
    assertTrue( orCondition.matches( object ) );
  }

  @Test
  public void testOneTrue() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( false );
    when( delegate2.matches( object ) ).thenReturn( true );
    assertTrue( orCondition.matches( object ) );
  }

  @Test
  public void testBothFalse() {
    Object object = new Object();
    when( delegate1.matches( object ) ).thenReturn( false );
    when( delegate2.matches( object ) ).thenReturn( false );
    assertFalse( orCondition.matches( object ) );
  }
}
