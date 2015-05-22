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

package org.pentaho.caching.ri.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
public class GuavaCacheManagerTest {

  public static final String CACHE_NAME = "TYPED_CACHE";

  @Rule public ExpectedException thrown = ExpectedException.none();

  private GuavaCacheManager cacheManager;

  @Before
  public void setUp() throws Exception {
    cacheManager = new GuavaCacheManager();
  }

  @Test
  public void testNewCache() throws Exception {
    MutableConfiguration<String, Map> configuration = new MutableConfiguration<String, Map>();
    configuration.setTypes( String.class, Map.class );
    Cache<String, Map> cache = cacheManager.createCache( CACHE_NAME, configuration );

    assertThat( cache.getConfiguration( Configuration.class ), sameInstance( (Configuration) configuration ) );
    assertThat( cache.getName(), is( CACHE_NAME ) );
    assertThat( cacheManager.getCacheNames(), contains( CACHE_NAME ) );

    cache.close();

    assertThat( cache.isClosed(), is( true ) );
    assertThat( cacheManager.getCacheNames(), emptyIterable() );
  }
}
