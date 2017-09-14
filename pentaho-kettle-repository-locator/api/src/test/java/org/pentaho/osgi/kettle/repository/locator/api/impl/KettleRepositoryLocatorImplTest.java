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

package org.pentaho.osgi.kettle.repository.locator.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.Repository;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryProvider;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 4/15/16.
 */
public class KettleRepositoryLocatorImplTest {
  private KettleRepositoryLocatorImpl kettleRepositoryLocator;

  @Before
  public void setup() {
    kettleRepositoryLocator = new KettleRepositoryLocatorImpl();
  }

  @Test
  public void testGetRepositoryNone() {
    assertNull( kettleRepositoryLocator.getRepository() );
  }

  @Test
  public void testGetRepositorySingleNull() {
    KettleRepositoryProvider provider = mock( KettleRepositoryProvider.class );
    kettleRepositoryLocator.itemAdded( provider, null );
    assertNull( kettleRepositoryLocator.getRepository() );
    verify( provider ).getRepository();
  }

  @Test
  public void testGetRepositoryMultiple() {
    KettleRepositoryProvider provider1 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider2 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider3 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider4 = mock( KettleRepositoryProvider.class );
    kettleRepositoryLocator
      .itemAdded( provider1, Collections.singletonMap( KettleRepositoryLocatorImpl.SERVICE_RANKING, 30 ) );
    kettleRepositoryLocator
      .itemAdded( provider2, Collections.singletonMap( KettleRepositoryLocatorImpl.SERVICE_RANKING, 40 ) );
    kettleRepositoryLocator
      .itemAdded( provider3, Collections.singletonMap( KettleRepositoryLocatorImpl.SERVICE_RANKING, 50 ) );
    kettleRepositoryLocator
      .itemAdded( provider4, Collections.singletonMap( KettleRepositoryLocatorImpl.SERVICE_RANKING, 20 ) );

    Repository repository = mock( Repository.class );
    when( provider1.getRepository() ).thenReturn( repository );

    assertEquals( repository, kettleRepositoryLocator.getRepository() );
    verify( provider1 ).getRepository();
    verify( provider2 ).getRepository();
    verify( provider3 ).getRepository();
    verify( provider4, never() ).getRepository();
  }
}
