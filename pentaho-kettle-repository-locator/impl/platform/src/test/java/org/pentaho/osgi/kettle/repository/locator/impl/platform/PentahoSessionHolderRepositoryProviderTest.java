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

package org.pentaho.osgi.kettle.repository.locator.impl.platform;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.repository.Repository;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/15/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoSessionHolderRepositoryProviderTest {
  public static final String SESSION_TEST_NAME = "sessionTestName";
  @Mock Supplier<IPentahoSession> pentahoSessionSupplier;
  @Mock IPentahoSession pentahoSession;
  @Mock Function<IPentahoSession, ICacheManager> cacheManagerFunction;
  @Mock ICacheManager cacheManager;

  private PentahoSessionHolderRepositoryProvider
    pentahoSessionHolderRepositoryProvider;

  @Before
  public void setup() {
    when( cacheManagerFunction.apply( pentahoSession ) ).thenReturn( cacheManager );
    when( pentahoSession.getName() ).thenReturn( SESSION_TEST_NAME );
    pentahoSessionHolderRepositoryProvider =
      new PentahoSessionHolderRepositoryProvider( pentahoSessionSupplier, cacheManagerFunction );
  }

  @Test
  public void testNoArgConstructor() {
    assertNotNull( new PentahoSessionHolderRepositoryProvider() );
  }

  @Test
  public void testGetRepositoryNullSession() {
    when( pentahoSessionSupplier.get() ).thenReturn(null );
    assertNull( pentahoSessionHolderRepositoryProvider.getRepository() );
  }

  @Test
  public void testGetRepositoryNull() {
    when( pentahoSessionSupplier.get() ).thenReturn( pentahoSession );
    when( cacheManager.getFromRegionCache( PentahoSessionHolderRepositoryProvider.REGION, SESSION_TEST_NAME ) ).thenReturn( null );
    assertNull( pentahoSessionHolderRepositoryProvider.getRepository() );
  }

  @Test
  public void testGetRepositorySuccess() {
    Repository repository = mock( Repository.class );
    when( pentahoSessionSupplier.get() ).thenReturn( pentahoSession );
    when( cacheManager.getFromRegionCache( PentahoSessionHolderRepositoryProvider.REGION, SESSION_TEST_NAME ) )
      .thenReturn( repository );
    assertEquals( repository, pentahoSessionHolderRepositoryProvider.getRepository() );
  }
}
