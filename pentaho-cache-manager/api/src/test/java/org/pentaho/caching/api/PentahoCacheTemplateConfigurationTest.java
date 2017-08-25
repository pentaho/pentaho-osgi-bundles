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
