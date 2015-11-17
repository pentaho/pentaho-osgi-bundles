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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.js.require;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class RequireJsDependencyResolverTest {
  private static JSONParser parser;

  static {
    parser = new JSONParser();
  }

  @Test
  public void testConfigFromBowerJson() throws IOException, ParseException {
    JSONObject json =
        (JSONObject) parser.parse(
            new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "require.meta.json" ) ) );

    RequireJsDependencyResolver.processMetaInformation( json );

    assertEquals( parser
            .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "require.resolved.json" )
            ) ),
        json );
  }
}
