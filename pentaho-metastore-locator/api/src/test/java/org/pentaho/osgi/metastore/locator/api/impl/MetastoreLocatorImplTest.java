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

package org.pentaho.osgi.metastore.locator.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.blueprint.collection.utils.ServiceMap;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;

/**
 * Created by tkafalas 7/26/2017.
 */
public class MetastoreLocatorImplTest {
  private MetastoreLocatorImpl metastoreLocator;

  @Before
  public void setup() {
    metastoreLocator = new MetastoreLocatorImpl();
  }

  @Test
  public void testgetMetastoreNone() {
    assertNull( metastoreLocator.getExplicitMetastore( "" ) );
  }

  @Test
  public void testGetMetastoreSingleNull() {
    // Test a null metastore provider that delivers a null metastore
    MetastoreProvider provider = mock( MetastoreProvider.class );
    metastoreLocator.itemAdded( provider, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY,
        MetastoreLocator.LOCAL_PROVIDER_KEY ) );
    assertNull( metastoreLocator.getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY ) );
    verify( provider ).getMetastore();
  }

  @Test
  public void testGetMetastoreTest() {
    //Test that repository metastore gets returned if both local and repository metastore providers exist.
    //Also test that both providers can be accessed directly.
    MetastoreProvider local_provider = mock( MetastoreProvider.class );
    IMetaStore local_meta = mock( IMetaStore.class );
    when( local_provider.getMetastore() ).thenReturn( local_meta );
    MetastoreProvider repo_provider = mock( MetastoreProvider.class );
    IMetaStore repo_meta = mock( IMetaStore.class );
    when( repo_provider.getMetastore() ).thenReturn( repo_meta );
    metastoreLocator.itemAdded( local_provider, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY,
        MetastoreLocator.LOCAL_PROVIDER_KEY ) );

    assertEquals( local_meta, metastoreLocator.getMetastore() );

    metastoreLocator.itemAdded( repo_provider, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY,
        MetastoreLocator.REPOSITORY_PROVIDER_KEY ) );
    assertEquals( repo_meta, metastoreLocator.getMetastore() );

    assertEquals( local_meta, metastoreLocator.getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY ) );
    assertEquals( repo_meta, metastoreLocator.getExplicitMetastore( MetastoreLocator.REPOSITORY_PROVIDER_KEY ) );
  }

  @Test
  public void testSetAndDisposeEmbeddedMetastore() {
    IMetaStore embedded_meta = mock( IMetaStore.class );
    String key = metastoreLocator.setEmbeddedMetastore( embedded_meta );
    assertNotNull( key, "Embedded key value not returned" );
    assertEquals( embedded_meta, metastoreLocator.getExplicitMetastore( key ) );
    assertEquals( embedded_meta, metastoreLocator.getMetastore( key ) );

    metastoreLocator.disposeMetastoreProvider( key );
    assertNull( metastoreLocator.getExplicitMetastore( key ) );
  }
}
