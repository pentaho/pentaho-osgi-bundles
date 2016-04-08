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
 * Copyright 2014-2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.js.require;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class RequireJsGeneratorTest {
  private static JSONParser parser;

  static {
    parser = new JSONParser();
  }

  @Test
  public void testConfigFromPom()
      throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parsePom( this.getClass().getClassLoader().getResourceAsStream(
        "pom.require.xml" ) );

    RequireJsGenerator.ArtifactInfo artifactInfo =
        new RequireJsGenerator.ArtifactInfo( "org.webjars", "smart-table", "2.0.3-1" );

    assertEquals( parser
            .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "pom.require.json" )
            ) ),
        moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }

  @Test
  public void testConfigFromJsScript()
      throws IOException, NoSuchMethodException, ScriptException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.processJsScript( "angularjs", "1.3.0-rc.0", this.getClass().getClassLoader().getResourceAsStream(
        "webjars-requirejs.js" ) );

    RequireJsGenerator.ArtifactInfo artifactInfo =
        new RequireJsGenerator.ArtifactInfo( "org.webjars", "angularjs", "1.3.0-rc.0" );

    assertEquals( parser
            .parse( new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream( "webjars-requirejs.require.json" )
            ) ),
        moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }

  @Test
  public void testConfigFromPackageJson() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
        "package.json" ) );

    RequireJsGenerator.ArtifactInfo artifactInfo =
        new RequireJsGenerator.ArtifactInfo( "org.webjars.npm", "asap", "2.0.3" );

    assertEquals( parser
            .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream(
                "package.require.json" )
            ) ),
        moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }

  @Test
  public void testConfigFromBowerJson() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
        "bower.json" ) );

    RequireJsGenerator.ArtifactInfo artifactInfo =
        new RequireJsGenerator.ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals( parser
            .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "bower.require.json" )
            ) ),
        moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }
}
