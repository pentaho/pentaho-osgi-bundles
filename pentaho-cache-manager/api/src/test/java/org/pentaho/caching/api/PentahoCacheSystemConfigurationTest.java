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
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PentahoCacheSystemConfigurationTest {

  private final Matcher<Map<String, String>> isEmptyMap = equalTo( Collections.<String, String>emptyMap() );
  @Mock private PentahoCacheManager cacheManager;
  @Mock private Configuration<java.lang.String, java.lang.Object> cacheConfiguration;
  @Mock private Cache<java.lang.String, java.lang.Object> cache;

  @Test
  public void testSetConfig() throws Exception {
    PentahoCacheSystemConfiguration systemConfiguration = new PentahoCacheSystemConfiguration();

    ImmutableMap.Builder<String, String> configBuilder = ImmutableMap.builder();
    configBuilder
      .put( "private", "private-value" )
      .put( "global.common", "common-value" )
      .put( "global.override", "global-override-value" )
      .put( "template.default.override", "default-override-value" )
      .put( "template.first", "First Template Description" )
      .put( "template.first.template-prop", "template private value" )
      .put( "template.first.override", "template-override-value" )
      .put( "template.second.template-prop", "second template private value" );

    assertThat( systemConfiguration.getGlobalProperties(), isEmptyMap );
    assertThat( systemConfiguration.createTemplates( cacheManager ).entrySet(), empty() );

    systemConfiguration.setData( configBuilder.build() );

    assertThat( systemConfiguration.getConfiguration().get( "private" ), equalTo( "private-value" ) );

    assertThat( systemConfiguration.getGlobalProperties(), Matchers.<Map<String, String>>equalTo( ImmutableMap.of(
      "common", "common-value",
      "override", "global-override-value"
    ) ) );

    Map<String, PentahoCacheTemplateConfiguration> templates = systemConfiguration.createTemplates( cacheManager );
    assertThat( templates.keySet(), containsInAnyOrder( "first", Constants.DEFAULT_TEMPLATE ) );

    PentahoCacheTemplateConfiguration defaultTemplate = templates.get( Constants.DEFAULT_TEMPLATE );
    assertThat( defaultTemplate.getDescription(), equalTo( Constants.DEFAULT_TEMPLATE_DESCRIPTION ) );
    assertThat( defaultTemplate.getProperties(), Matchers.<Map<String, String>>equalTo( ImmutableMap.of(
      "common", "common-value",
      "override", "default-override-value"
    ) ) );

    PentahoCacheTemplateConfiguration firstTemplate = templates.get( "first" );
    assertThat( firstTemplate.getDescription(), equalTo( "First Template Description" ) );
    assertThat( firstTemplate.getCacheManager(), equalTo( cacheManager ) );

    assertThat( firstTemplate.getProperties(), Matchers.<Map<String, String>>equalTo( ImmutableMap.of(
      "common", "common-value",
      "override", "template-override-value",
      "template-prop", "template private value"
    ) ) );

    when( cacheManager.createConfiguration( String.class, Object.class, firstTemplate.getProperties() ) )
      .thenReturn( cacheConfiguration );
    when( cacheManager.createCache( "cacheName", cacheConfiguration ) ).thenReturn( cache );

    assertThat( firstTemplate.createCache( "cacheName", String.class, Object.class ), equalTo( cache ) );
  }
}
