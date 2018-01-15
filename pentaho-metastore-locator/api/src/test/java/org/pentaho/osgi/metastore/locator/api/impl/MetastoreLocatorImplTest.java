/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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
