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
package org.pentaho.js.require;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RequireJsDependencyResolverTest {
  private static JSONParser parser;

  static {
    parser = new JSONParser();
  }

  @Test
  public void testProcessMetaInformation() throws IOException, ParseException {
    final InputStreamReader requireReader = new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "require.meta.json" ) );

    Object requireConfig = parser.parse( requireReader );
    RequireJsDependencyResolver.processMetaInformation( (Map<String, Object>) requireConfig );

    final InputStreamReader expectedReader = new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "require.resolved.json" ) );
    final Object expectedRequireConfig = parser.parse( expectedReader );

    assertEquals( expectedRequireConfig, requireConfig );
  }
}
