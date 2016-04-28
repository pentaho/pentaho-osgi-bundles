/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.osgi.metastore.locator.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 4/15/16.
 */
public class MetastoreLocatorImplTest {
  private MetastoreLocatorImpl metastoreLocator;

  @Before
  public void setup() {
    metastoreLocator = new MetastoreLocatorImpl();
  }

  @Test
  public void testgetMetastoreNone() {
    assertNull( metastoreLocator.getMetastore() );
  }

  @Test
  public void testgetMetastoreSingleNull() {
    MetastoreProvider provider = mock( MetastoreProvider.class );
    metastoreLocator.itemAdded( provider, null );
    assertNull( metastoreLocator.getMetastore() );
    verify( provider ).getMetastore();
  }

  @Test
  public void testgetMetastoreMultiple() {
    MetastoreProvider provider1 = mock( MetastoreProvider.class );
    MetastoreProvider provider2 = mock( MetastoreProvider.class );
    MetastoreProvider provider3 = mock( MetastoreProvider.class );
    MetastoreProvider provider4 = mock( MetastoreProvider.class );
    metastoreLocator
      .itemAdded( provider1, Collections.singletonMap( MetastoreLocatorImpl.SERVICE_RANKING, 30 ) );
    metastoreLocator
      .itemAdded( provider2, Collections.singletonMap( MetastoreLocatorImpl.SERVICE_RANKING, 40 ) );
    metastoreLocator
      .itemAdded( provider3, Collections.singletonMap( MetastoreLocatorImpl.SERVICE_RANKING, 50 ) );
    metastoreLocator
      .itemAdded( provider4, Collections.singletonMap( MetastoreLocatorImpl.SERVICE_RANKING, 20 ) );

    IMetaStore metaStore = mock( IMetaStore.class );
    when( provider1.getMetastore() ).thenReturn( metaStore );

    assertEquals( metaStore, metastoreLocator.getMetastore() );
    verify( provider1 ).getMetastore();
    verify( provider2 ).getMetastore();
    verify( provider3 ).getMetastore();
    verify( provider4, never() ).getMetastore();
  }
}
