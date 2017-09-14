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

package org.pentaho.osgi.kettle.repository.locator.impl.spoon;

import org.junit.Test;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/15/16.
 */
public class SpoonRepositoryProviderTest {
  @Test
  public void testNoArgConstructor() {
    assertNotNull( new SpoonRepositoryProvider() );
  }

  @Test
  public void testGetRepository() {
    Spoon spoon = mock( Spoon.class );
    Repository repository = mock( Repository.class );
    when( spoon.getRepository() ).thenReturn( repository );
    assertEquals( repository, new SpoonRepositoryProvider( () -> spoon ).getRepository() );
  }
}
