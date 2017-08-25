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

package org.pentaho.osgi.metastore.locator.impl.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryLocator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/18/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class RepositoryMetastoreProviderTest {
  @Mock KettleRepositoryLocator kettleRepositoryLocator;
  private RepositoryMetastoreProvider repositoryMetastoreProvider;

  @Before
  public void setup() {
    repositoryMetastoreProvider = new RepositoryMetastoreProvider( kettleRepositoryLocator );
  }

  @Test
  public void testGetMetastoreNullRepository() {
    when( kettleRepositoryLocator.getRepository() ).thenReturn( null );
    assertNull( repositoryMetastoreProvider.getMetastore() );
  }

  @Test
  public void testGetMetastoreSuccess() {
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    when( repository.getMetaStore() ).thenReturn( metaStore );
    when( kettleRepositoryLocator.getRepository() ).thenReturn( repository );
    assertEquals( metaStore, repositoryMetastoreProvider.getMetastore() );
  }
}
