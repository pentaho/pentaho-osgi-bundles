/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.caching.ri;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.ri.impl.GuavaCacheManager;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class HeapCacheProvidingServiceTest {

  @Mock private PentahoCacheSystemConfiguration config;
  private HeapCacheProvidingService service;

  @Before
  public void setUp() throws Exception {
    service = new HeapCacheProvidingService();
  }

  @Test
  public void testCreateCacheManager() throws Exception {
    CacheManager cacheManager = service.createCacheManager( config );
    assertThat( cacheManager, instanceOf( GuavaCacheManager.class ) );
  }

  @Test
  public void testCreateConfiguration() throws Exception {
    HashMap<String, String> properties = Maps.newHashMap();
    Configuration<String, List> configuration = service.createConfiguration( String.class, List.class, properties );
    assertThat( configuration.getKeyType(), equalTo( String.class ) );
    assertThat( configuration.getValueType(), equalTo( List.class ) );
  }
}
