/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WebjarsURLConnectionTest {
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
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ), true );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table@2.0.3-1", "smart-table/2.0.3-1" );
    verifyRequireJson( zipInputStream, "org.webjars/smart-table", "2.0.3-1" );
  }

  @Test
  public void testNoSourceFolder() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1-no-source-folder" ), true );

    verifyManifest( zipInputStream );
    verifyNoRequireJson( zipInputStream );
    verifyNoBlueprint( zipInputStream );
  }

  @Test
  public void testNoResourceFolder() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1-no-resource-folder" ), true );

    verifyManifest( zipInputStream );
    verifyNoRequireJson( zipInputStream );
    verifyNoBlueprint( zipInputStream );
  }

  @Test
  public void testClassicWebjarScriptedConfig() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/jquery/2.2.1" ), true );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "jquery@2.2.1", "jquery/2.2.1" );
    verifyRequireJson( zipInputStream, "org.webjars/jquery", "2.2.1" );
  }

  @Test
  public void testNpmWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.npm/asap/2.0.3" ), true );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "asap@2.0.3", "asap/2.0.3"  );
    verifyRequireJson( zipInputStream, "org.webjars.npm/asap", "2.0.3" );
  }

  @Test
  public void testBowerWebjar() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.bower/angular-ui-router.stateHelper/1.3.1" ), true );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-ui-router.stateHelper@1.3.1", "angular-ui-router.stateHelper/1.3.1" );
    verifyRequireJson( zipInputStream, "org.webjars.bower/angular-ui-router.stateHelper", "1.3.1" );
  }

  @Test
  public void testMalformedWebjarFallback() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ), true );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-dateparser@1.0.9", "angular-dateparser/1.0.9" );
    verifyRequireJson( zipInputStream, "org.webjars/angular-dateparser", "1.0.9" );
  }

  @Test
  public void testClosingStream() throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ), false, false );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    JarInputStream jar = new JarInputStream( inputStream );
    jar.getManifest();
    jar.close();
    try {
      connection.transform_thread.get();
    } catch ( Exception exception ) {
      fail( "Thread failed to execute transform() method: " + exception.getMessage() );
    }
  }

  @Test( expected = IOException.class )
  public void testInputStreamException() throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9xx" ), false, false );
    connection.connect();

    connection.getInputStream();
  }

  @Test
  public void testMinifiedResources() throws IOException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ), true );

    verifyBlueprint( zipInputStream, "smart-table@2.0.3-1", "smart-table/2.0.3-1" );

    verifyMinified( zipInputStream, "smart-table/2.0.3-1", "smart-table.js" );
  }

  @Test
  public void testFailedMinification() throws IOException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1-fail-minification" ), true );

    verifyBlueprint( zipInputStream, "smart-table@2.0.3-1", "smart-table/2.0.3-1" );

    verifyNotMinified( zipInputStream, "smart-table/2.0.3-1", "smart-table.js" );
  }

  @Test
  public void testDisabledMinification() throws IOException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ), false );

    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull( entry );

    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    Pattern distPattern = Pattern.compile( "<service id=\"resourcesDist\".*>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.pattern\" value=\"\\/smart-table@2.0.3-1\\/\\*\"\\/>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.prefix\" value=\"\\/META-INF\\/resources\\/dist-gen\"\\/>.*" +
        "<\\/service>", Pattern.DOTALL );

    Matcher matcher = distPattern.matcher( bpFile );

    assertFalse( "blueprint.xml shouldn't include path for minified smart-table", matcher.find() );

    distPattern = Pattern.compile( "<service id=\"resourcesSrc\".*>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.pattern\" value=\"\\/webjar-src\\/smart-table@2.0.3-1\\/\\*\"\\/>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.prefix\" value=\"\\/META-INF\\/resources\\/webjars\\/smart-table/2.0.3-1\"\\/>.*" +
        "<\\/service>", Pattern.DOTALL );

    matcher = distPattern.matcher( bpFile );

    assertFalse( "blueprint.xml shouldn't include path for smart-table sources", matcher.find() );

    distPattern = Pattern.compile( "<service id=\"resourcesDist\".*>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.pattern\" value=\"\\/smart-table@2.0.3-1\\/\\*\"\\/>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.prefix\" value=\"\\/META-INF\\/resources\\/webjars\\/smart-table/2.0.3-1\"\\/>.*" +
        "<\\/service>", Pattern.DOTALL );

    matcher = distPattern.matcher( bpFile );

    assertTrue( "blueprint.xml should include path for original smart-table", matcher.find() );

    entry = zipInputStream.getEntry( "META-INF/resources/dist-gen/smart-table.js" );
    assertNull( entry );

    entry = zipInputStream.getEntry( "META-INF/resources/webjars/smart-table/2.0.3-1/smart-table.js");
    assertNotNull( entry );
  }

  @Test
  public void testWrappedResources() throws IOException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/test/1.0.0" ), true );

    verifyWrapped( zipInputStream, "test/1.0.0", "dist/test.js", "// CODE BEFORE", "// CODE AFTER" );
  }

  private void verifyManifest( ZipFile zipInputStream ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull( entry );
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( "Bundle-SymbolicName is not pentaho-webjars-",
        manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).startsWith( "pentaho-webjars-" ) );
  }

  private void verifyBlueprint( ZipFile zipInputStream, String expectedAlias, String expectedPath ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull( entry );

    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    Pattern distPattern = Pattern.compile( "<service id=\"resourcesDist\".*>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.pattern\" value=\"\\/" + expectedAlias + "\\/\\*\"\\/>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.prefix\" value=\"\\/META-INF\\/resources\\/dist-gen\"\\/>.*" +
        "<\\/service>", Pattern.DOTALL );

    Matcher matcher = distPattern.matcher( bpFile );

    assertTrue( "blueprint.xml does not include path for minified " + expectedPath, matcher.find() );

    distPattern = Pattern.compile( "<service id=\"resourcesSrc\".*>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.pattern\" value=\"\\/webjar-src\\/" + expectedAlias + "\\/\\*\"\\/>.*" +
        "<entry key=\"osgi\\.http\\.whiteboard\\.resource\\.prefix\" value=\"\\/META-INF\\/resources\\/webjars\\/" + expectedPath + "\"\\/>.*" +
        "<\\/service>", Pattern.DOTALL );

    matcher = distPattern.matcher( bpFile );

    assertTrue( "blueprint.xml does not include path for " + expectedPath + " " + bpFile + " sources", matcher.find() );
  }

  private void verifyNoBlueprint( ZipFile zipInputStream ) {
    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNull( entry );
  }

  private void verifyRequireJson( ZipFile zipInputStream, String artifactId, String version ) throws IOException, ParseException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull( entry );

    String jsonFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    JSONObject json = (JSONObject) (new JSONParser()).parse( jsonFile );

    assertTrue( "dependency metadata exists", json.containsKey( "requirejs-osgi-meta" ) );
    final JSONObject meta = (JSONObject) json.get( "requirejs-osgi-meta" );

    assertTrue( "artifact info exists", meta.containsKey( "artifacts" ) );
    final JSONObject artifactInfo = (JSONObject) meta.get( "artifacts" );

    assertTrue( "artifact is " + artifactId, artifactInfo.containsKey( artifactId ) );
    final JSONObject versionInfo = (JSONObject) artifactInfo.get( artifactId );

    assertTrue( "version is " + version, versionInfo.containsKey( version ) );
  }

  private void verifyNoRequireJson( ZipFile zipInputStream ) {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNull( entry );
  }

  private void verifyMinified( ZipFile zipInputStream, String expectedPath, String file ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/resources/dist-gen/" + file );
    assertNotNull( entry );

    String minFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    entry = zipInputStream.getEntry( "META-INF/resources/webjars/" + expectedPath + "/" + file );
    assertNotNull( entry );

    String srcFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    assertNotEquals( minFile, srcFile );
  }

  private void verifyNotMinified( ZipFile zipInputStream, String expectedPath, String file ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/resources/dist-gen/" + file );
    assertNotNull( entry );

    String minFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    entry = zipInputStream.getEntry( "META-INF/resources/webjars/" + expectedPath + "/" + file );
    assertNotNull( entry );

    String srcFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    assertEquals( minFile, srcFile );
  }

  private void verifyWrapped( ZipFile zipInputStream, String expectedPath, String file, String pre, String pos ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/resources/webjars/" + expectedPath + "/" + file );
    assertNotNull( entry );

    String srcFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    assertTrue( srcFile.startsWith( pre + "\n" ) );
    assertTrue( srcFile.endsWith( "\n" + pos + "\n" ) );
  }

  private ZipFile getDeployedJar( URL webjar_url, boolean minificationEnabled ) throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( webjar_url, minificationEnabled, false );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test", ".zip" );

    FileOutputStream fileOutputStream = new FileOutputStream( tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    return new ZipFile( tempFile );
  }
}
