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

package org.pentaho.osgi.metastore.locator.impl.local;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/18/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class LocalFileMetastoreProviderTest {
  @Mock LocalFileMetastoreProvider.MetastoreSupplier supplier;
  private LocalFileMetastoreProvider localFileMetastoreProvider;

  @Before
  public void setup() {
    localFileMetastoreProvider = new LocalFileMetastoreProvider( supplier );
  }

  @Test
  public void testNoArgConstructor() {
    assertNotNull( new LocalFileMetastoreProvider() );
  }

  @Test
  public void testGetMetastoreException() throws MetaStoreException {
    when( supplier.getMetastore() ).thenThrow( new MetaStoreException() );
    assertNull( localFileMetastoreProvider.getMetastore() );
  }

  @Test
  public void testGetMetastoreSuccess() throws MetaStoreException {
    IMetaStore metaStore = mock( IMetaStore.class );
    when( supplier.getMetastore() ).thenReturn( metaStore );
    assertEquals( metaStore, localFileMetastoreProvider.getMetastore() );
  }
}
