/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
