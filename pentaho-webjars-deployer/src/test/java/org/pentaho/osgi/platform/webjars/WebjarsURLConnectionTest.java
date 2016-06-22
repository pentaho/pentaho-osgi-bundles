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

package org.pentaho.osgi.platform.webjars;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class WebjarsURLConnectionTest {
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
  public void testClassicWebjarPomConfig() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table/2.0.3-1" );
    verifyRequireJson( zipInputStream, "org.webjars/smart-table", "2.0.3-1" );
  }

  @Test
  public void testClassicWebjarAltered() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table/2.0.3-1" );
    verifyRequireJsonFalse( zipInputStream, "org.webjars/smart-table", "2.0.3-1a" );
  }

  @Test
  public void testClassicWebjarNoResources() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table/2.0.3-1" );
    verifyRequireJsonFalse( zipInputStream, "org.webjars/smart-table", "2.0.3-1b" );
  }

  @Test
  public void testClassicWebjarScriptedConfig() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angularjs/1.3.0-rc.0" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angularjs/1.3.0-rc.0" );
    verifyRequireJson( zipInputStream, "org.webjars/angularjs", "1.3.0-rc.0" );
  }

  @Test
  public void testNpmWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.npm/asap/2.0.3" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "asap/2.0.3" );
    verifyRequireJson( zipInputStream, "org.webjars.npm/asap", "2.0.3" );
  }

  @Test
  public void testBowerWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.bower/angular-ui-router.stateHelper/1.3.1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-ui-router.stateHelper/1.3.1" );
    verifyRequireJson( zipInputStream, "org.webjars.bower/angular-ui-router.stateHelper", "1.3.1" );
  }

  @Test
  public void testMalformedWebjarFallback() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-dateparser/1.0.9" );
    verifyRequireJson( zipInputStream, "org.webjars/angular-dateparser", "1.0.9" );
  }

  @Test
  public void testClosingStream() throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ) );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    JarInputStream jar = new JarInputStream( inputStream );
    jar.getManifest();
    jar.close();
    try {
      connection.transform_thread.get();
    } catch ( Exception exception ) {
      fail( "Thread failed to execute tranform() method: " + exception.getMessage() );
    }
  }

  @Test
  public void testInputStreamException() throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9xx" ) );
    connection.connect();

    try {
      InputStream inputStream = connection.getInputStream();
    } catch ( Exception exception ) {
      fail( "Thread failed to execute tranform() method: " + exception.getMessage() );
    }
  }

  private void verifyManifest( ZipFile zipInputStream ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull( entry );
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( "Bundle-SymbolicName is not pentaho-webjars-",
        manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).startsWith( "pentaho-webjars-" ) );
  }

  private void verifyBlueprint( ZipFile zipInputStream, String expectedPath ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull( entry );

    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( "blueprint.xml does not include path for " + expectedPath,
        bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/" + expectedPath + "\" />" ) );
  }

  private void verifyRequireJson( ZipFile zipInputStream, String artifactId, String version ) throws IOException, ParseException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull( entry );

    String jsonFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    JSONObject json = (JSONObject) parser.parse( jsonFile );

    assertTrue( "dependency metadata exists", json.containsKey( "requirejs-osgi-meta" ) );
    final JSONObject meta = (JSONObject) json.get( "requirejs-osgi-meta" );

    assertTrue( "artifact info exists", meta.containsKey( "artifacts" ) );
    final JSONObject artifactInfo = (JSONObject) meta.get( "artifacts" );

    assertTrue( "artifact is " + artifactId, artifactInfo.containsKey( artifactId ) );
    final JSONObject versionInfo = (JSONObject) artifactInfo.get( artifactId );

    assertTrue( "version is " + version, versionInfo.containsKey( version ) );
  }

  private void verifyRequireJsonFalse( ZipFile zipInputStream, String artifactId, String version ) throws IOException, ParseException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull( entry );

    String jsonFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    JSONObject json = (JSONObject) parser.parse( jsonFile );

    assertTrue( "dependency metadata exists", json.containsKey( "requirejs-osgi-meta" ) );
    final JSONObject meta = (JSONObject) json.get( "requirejs-osgi-meta" );

    assertTrue( "artifact info exists", meta.containsKey( "artifacts" ) );
    final JSONObject artifactInfo = (JSONObject) meta.get( "artifacts" );

    assertTrue( "artifact is " + artifactId, artifactInfo.containsKey( artifactId ) );
    final JSONObject versionInfo = (JSONObject) artifactInfo.get( artifactId );

    assertFalse( "version is " + version, versionInfo.containsKey( version ) );
  }

  private ZipFile getDeployedJar( URL webjar_url ) throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( webjar_url );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test", ".zip" );

    FileOutputStream fileOutputStream = new FileOutputStream( tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    return new ZipFile( tempFile );
  }
}
