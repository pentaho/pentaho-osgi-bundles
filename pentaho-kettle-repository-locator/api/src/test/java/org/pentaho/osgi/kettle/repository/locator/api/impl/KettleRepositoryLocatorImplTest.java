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
