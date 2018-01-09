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
 * Copyright 2014 - 2018 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.js.require;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequireJsGeneratorTest {
  private static JSONParser parser;

  static {
    parser = new JSONParser();
  }

  @Before
  public void before() throws MalformedURLException {
    File input = new File( "src/test/resources/mockRepo" );

    System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
    System.setProperty( "org.ops4j.pax.url.mvn.repositories",
            input.toURI().toURL().toString() + "@snapshots@id=mock-repo" );
    System.setProperty( "org.ops4j.pax.url.mvn.localRepository", input.toURI().toURL().toString() );
    System.setProperty( "org.ops4j.pax.url.mvn.proxySupport", "false" );
  }

  @Test
  public void testEmptyGenerator() throws IOException, ParseException {
    RequireJsGenerator emptyGenerator = RequireJsGenerator.emptyGenerator( "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals( "angular-ui-router.stateHelper" , emptyGenerator.getModuleInfo().getName() );
    assertEquals( "1.3.1" , emptyGenerator.getModuleInfo().getVersion() );
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

  @Test
  public void testConfigFromBowerJsonNoVersion() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
            "bower_no_version.json" ) );

    assertNull(moduleInfo.getModuleInfo().getVersion());

    moduleInfo.getModuleInfo().setVersion( "1.3.1" );

    assertEquals("1.3.1", moduleInfo.getModuleInfo().getVersion());

    RequireJsGenerator.ArtifactInfo artifactInfo =
            new RequireJsGenerator.ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals( parser
                    .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "bower_no_version.require.json" )
                    ) ),
            moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }

  @Test
  public void testModuleInfoSetName() throws IOException, ParseException {
      RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
              "bower.json" ) );

      assertEquals( "angular-ui-router.stateHelper", moduleInfo.getModuleInfo().getName());
      moduleInfo.getModuleInfo().setName( "angular-ui-router" );

      assertEquals( "angular-ui-router", moduleInfo.getModuleInfo().getName());
  }

  @Test
  public void testModuleInfoSetPath() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
            "bower.json" ) );

    moduleInfo.getModuleInfo().setPath( "angular-ui-router.stateHelper/1.3.1" );

    assertEquals( "angular-ui-router.stateHelper/1.3.1", moduleInfo.getModuleInfo().getPath());
  }

  @Test
  public void testModuleInfoGetExports() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
            "bower.json" ) );

    assertNull( moduleInfo.getModuleInfo().getExports());

    RequireJsGenerator.ArtifactInfo artifactInfo =
           new RequireJsGenerator.ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );
    assertEquals( parser
          .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "bower_exports.require.json" )
          ) ),
          moduleInfo.getConvertedConfig( artifactInfo, true, "test_export" ).getRequireJs() );
  }

    @Test
  public void testModuleInfoExportRequireJs() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( this.getClass().getClassLoader().getResourceAsStream(
            "bower.json" ) );

    assertNull( moduleInfo.getModuleInfo().getExports());

    RequireJsGenerator.ArtifactInfo artifactInfo =
            new RequireJsGenerator.ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    RequireJsGenerator.ModuleInfo infoConvertedFile = moduleInfo.getConvertedConfig( artifactInfo, true, "test_export" );

    String exportRequire = infoConvertedFile.exportRequireJs();

    assertEquals(  parser
            .parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream( "bower_exports.require.json" ) ) ),
            parser
                    .parse( exportRequire ) );
  }

  @Test
  public void testArtifactInfoURL()
          throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parsePom( this.getClass().getClassLoader().getResourceAsStream(
            "pom.require.xml" ) );

    RequireJsGenerator.ArtifactInfo artifactInfo =
            new RequireJsGenerator.ArtifactInfo( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    assertEquals( parser.parse( new InputStreamReader( this.getClass().getClassLoader().getResourceAsStream(
            "pom.require.json" ) ) ),
            moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );

    assertEquals( "2.0.3.1",
            artifactInfo.getOsgiCompatibleVersion() );
  }

  @Test
  public void testWebjarVersionFromPom()
          throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParseException {
    String version = RequireJsGenerator.getWebjarVersionFromPom( this.getClass().getClassLoader().getResourceAsStream(
            "pom.webjar.xml" ) );

    assertEquals( "3.1.1",
            version );
  }
}
