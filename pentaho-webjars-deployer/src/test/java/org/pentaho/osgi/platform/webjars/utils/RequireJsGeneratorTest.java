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

package org.pentaho.osgi.platform.webjars.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RequireJsGeneratorTest {

  private static final String POM_WEBJAR_XML = "pom.webjar.xml";
  private static final String POM_REQUIRE_XML = "pom.require.xml";
  private static final String POM_REQUIRE_JSON = "pom.require.json";

  private static final String WEBJARS_REQUIREJS_JS = "webjars-requirejs.js";
  private static final String WEBJARS_REQUIREJS_JSON = "webjars-requirejs.require.json";

  private static final String PACKAGE_JSON = "package.json";
  private static final String PACKAGE_REQUIRE_JSON = "package.require.json";

  private static final String BOWER_JSON = "bower.json";
  private static final String BOWER_REQUIRE_JSON = "bower.require.json";
  private static final String BOWER_EXPORTS_REQUIRE_JSON = "bower_exports.require.json";

  private static final String BOWER_NO_VERSION_JSON = "bower_no_version.json";
  private static final String BOWER_NO_VERSION_REQUIRE_JSON = "bower_no_version.require.json";

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
  public void testEmptyGenerator() {
    RequireJsGenerator emptyGenerator = RequireJsGenerator.emptyGenerator( "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals( "angular-ui-router.stateHelper" , emptyGenerator.getModuleInfo().getName() );
    assertEquals( "1.3.1" , emptyGenerator.getModuleInfo().getVersion() );
  }


  @Test
  public void testConfigFromPom() throws Exception {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parsePom( getResourceAsStream( POM_REQUIRE_XML ) );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars", "smart-table", "2.0.3-1" );

    assertEquals(
      getExpectedOutput( POM_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );
  }

  @Test
  public void testConfigFromJsScript() throws Exception {
    String name = "angularjs";
    String version = "1.3.0-rc.0";

    RequireJsGenerator moduleInfo = RequireJsGenerator
      .processJsScript( name, version, getResourceAsStream( WEBJARS_REQUIREJS_JS ) );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars", name, version );

    assertEquals(
      getExpectedOutput( WEBJARS_REQUIREJS_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );
  }

  @Test
  public void testConfigFromPackageJson() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( PACKAGE_JSON ) );
    assertNotNull( moduleInfo );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars.npm", "asap", "2.0.3" );

    assertEquals(
      getExpectedOutput( PACKAGE_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );
  }

  @Test
  public void testConfigFromBowerJson() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_JSON ) );
    assertNotNull( moduleInfo );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals(
      getExpectedOutput( BOWER_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );
  }

  @Test
  public void testConfigFromBowerJsonNoVersion() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_NO_VERSION_JSON ) );
    assertNotNull( moduleInfo );

    assertNull( moduleInfo.getModuleInfo().getVersion() );

    String version = "1.3.1";
    moduleInfo.getModuleInfo().setVersion( version );
    assertEquals(version, moduleInfo.getModuleInfo().getVersion());

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", version );

    assertEquals(
      getExpectedOutput( BOWER_NO_VERSION_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );
  }

  @Test
  public void testModuleInfoSetName() {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_JSON ) );
    assertNotNull( moduleInfo );

    assertEquals( "angular-ui-router.stateHelper", moduleInfo.getModuleInfo().getName() );

    String moduleName = "angular-ui-router";
    moduleInfo.getModuleInfo().setName( moduleName );
    assertEquals( moduleName, moduleInfo.getModuleInfo().getName() );
  }

  @Test
  public void testModuleInfoSetPath() {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_JSON ) );
    assertNotNull( moduleInfo );

    String expectedPath = "angular-ui-router.stateHelper/1.3.1";
    moduleInfo.getModuleInfo().setPath( expectedPath );

    assertEquals( expectedPath, moduleInfo.getModuleInfo().getPath());
  }

  @Test
  public void testModuleInfoGetExports() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_JSON ) );
    assertNotNull( moduleInfo );

    assertNull( moduleInfo.getModuleInfo().getExports() );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    assertEquals(
      getExpectedOutput( BOWER_EXPORTS_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo, true, "test_export" )
    );
  }

  @Test
  public void testModuleInfoExportRequireJs() throws IOException, ParseException {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parseJsonPackage( getResourceAsStream( BOWER_JSON ) );
    assertNotNull( moduleInfo );

    assertNull( moduleInfo.getModuleInfo().getExports() );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( "org.webjars.bower", "angular-ui-router.stateHelper", "1.3.1" );

    RequireJsGenerator.ModuleInfo infoConvertedFile = moduleInfo
      .getConvertedConfig( artifactInfo, true, "test_export" );

    assertEquals(
      getExpectedOutput( BOWER_EXPORTS_REQUIRE_JSON ),
      parser.parse( infoConvertedFile.exportRequireJs() )
    );
  }

  @Test
  public void testArtifactInfoURL() throws Exception {
    RequireJsGenerator moduleInfo = RequireJsGenerator.parsePom( getResourceAsStream( POM_REQUIRE_XML ) );

    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator
      .ArtifactInfo( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    assertEquals(
      getExpectedOutput( POM_REQUIRE_JSON ),
      getRequireJsConfig( moduleInfo, artifactInfo )
    );

    assertEquals( "2.0.3.1", artifactInfo.getOsgiCompatibleVersion() );
  }

  @Test
  public void testWebjarVersionFromPom() throws Exception {
    String version = RequireJsGenerator.getWebjarVersionFromPom( getResourceAsStream( POM_WEBJAR_XML ) );

    assertEquals( "3.1.1", version );
  }

  // region private methods
  private Object getExpectedOutput( String resource ) throws IOException, ParseException {
    return parser.parse( new InputStreamReader( getResourceAsStream( resource ) ) );
  }

  private InputStream getResourceAsStream( String resource ) {
    return this.getClass().getClassLoader().getResourceAsStream( resource );
  }

  private JSONObject getRequireJsConfig( RequireJsGenerator moduleInfo, RequireJsGenerator.ArtifactInfo artifactInfo ) {
    return new JSONObject( moduleInfo.getConvertedConfig( artifactInfo ).getRequireJs() );
  }

  private JSONObject getRequireJsConfig( RequireJsGenerator moduleInfo, RequireJsGenerator.ArtifactInfo artifactInfo,
                                         boolean isAmdPackage, String exports ) {
    return new JSONObject( moduleInfo.getConvertedConfig( artifactInfo, isAmdPackage, exports ).getRequireJs() );
  }
  // endregion

}
