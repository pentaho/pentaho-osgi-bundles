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
package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class PentahoCacheTemplateConfigurationTest {

  @Test public void testOverrideProperties() throws Exception {
    PentahoCacheTemplateConfiguration
        templateConfiguration =
        new PentahoCacheTemplateConfiguration( "description",
            ImmutableMap.of( "foo", "bar", "baz", "bop", "ttl", "60" ), mock( PentahoCacheManager.class ) );
    assertEquals( "60", templateConfiguration.getProperties().get( "ttl" ) );

    PentahoCacheTemplateConfiguration
        overriddenConfig =
        templateConfiguration.overrideProperties( ImmutableMap.of( "ttl", "120", "marco", "polo" ) );

    assertEquals( "120", overriddenConfig.getProperties().get( "ttl" ) );
    assertEquals( "polo", overriddenConfig.getProperties().get( "marco" ) );
    assertEquals( "bar", overriddenConfig.getProperties().get( "foo" ) );
    assertEquals( "bop", overriddenConfig.getProperties().get( "baz" ) );
  }

}
