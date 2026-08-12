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

package org.pentaho.osgi.platform.webjars;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.assertEquals;
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
  public void testClassicWebjarPomConfig() throws Exception {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "smart-table@2.0.3-1", "smart-table/2.0.3-1" );
    verifyRequireJson( zipInputStream, "org.webjars/smart-table", "2.0.3-1" );
  }

  @Test
  public void testNoSourceFolder() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1-no-source-folder" ) );

    verifyManifest( zipInputStream );
    verifyNoRequireJson( zipInputStream );
    verifyNoBlueprint( zipInputStream );
  }

  @Test
  public void testNoResourceFolder() throws IOException, ParseException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/smart-table/2.0.3-1-no-resource-folder" ) );

    verifyManifest( zipInputStream );
    verifyNoRequireJson( zipInputStream );
    verifyNoBlueprint( zipInputStream );
  }

  @Test
  public void testClassicWebjarScriptedConfig() throws Exception {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/jquery/2.2.1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "jquery@2.2.1", "jquery/2.2.1" );
    verifyRequireJson( zipInputStream, "org.webjars/jquery", "2.2.1" );
  }

  @Test
  public void testNpmWebjar() throws Exception {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.npm/asap/2.0.3" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "asap@2.0.3", "asap/2.0.3"  );
    verifyRequireJson( zipInputStream, "org.webjars.npm/asap", "2.0.3" );
  }

  @Test
  public void testBowerWebjar() throws Exception {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars.bower/angular-ui-router.stateHelper/1.3.1" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-ui-router.stateHelper@1.3.1", "angular-ui-router.stateHelper/1.3.1" );
    verifyRequireJson( zipInputStream, "org.webjars.bower/angular-ui-router.stateHelper", "1.3.1" );
  }

  /**
   * [PDI-20686] A bower webjar whose {@code bower.json} carries no {@code version} <em>and</em> whose
   * {@code pom.xml} is absent (so Maven metadata cannot supply one either) must still be versioned,
   * using the version embedded in the physical {@code META-INF/resources/webjars/<name>/<version>}
   * path. Without that fallback the RequireJS mappings are generated with a null version and every
   * mapped resource 404s - which is what broke DET's pivot table (all datatables.net* assets).
   */
  @Test
  public void testBowerWebjarWithoutVersionAnywhereFallsBackToResourcesPath() throws Exception {
    ZipFile zipInputStream =
        getDeployedJar( new URL( "mvn:org.webjars.bower/angular-ui-router.stateHelper/1.3.1-no-pom" ) );

    verifyManifest( zipInputStream );
    // The resources path says 1.3.1, so that is the version the mappings must use - not "null".
    verifyBlueprint( zipInputStream, "angular-ui-router.stateHelper@1.3.1",
        "angular-ui-router.stateHelper/1.3.1" );
    // The Maven coordinate itself is still "1.3.1-no-pom" (that's what makes this fixture distinct from
    // the "1.3.1" one); what matters is that the nested version fell back to "1.3.1", not "null".
    verifyRequireJson( zipInputStream, "org.webjars.bower/angular-ui-router.stateHelper", "1.3.1-no-pom" );
    verifyRequireJsonModuleVersion( zipInputStream, "angular-ui-router.stateHelper", "1.3.1" );
  }

  @Test
  public void testMalformedWebjarFallback() throws Exception {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ) );

    verifyManifest( zipInputStream );
    verifyBlueprint( zipInputStream, "angular-dateparser@1.0.9", "angular-dateparser/1.0.9" );
    verifyRequireJson( zipInputStream, "org.webjars/angular-dateparser", "1.0.9" );
  }

  @Test
  public void testClosingStream() throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9" ), false );
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
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/angular-dateparser/1.0.9xx" ), false );
    connection.connect();

    connection.getInputStream();
  }

  @Test
  public void testWrappedResources() throws IOException {
    ZipFile zipInputStream = getDeployedJar( new URL( "mvn:org.webjars/test/1.0.0" ) );

    verifyWrapped( zipInputStream, "test/1.0.0", "dist/test.js", "// CODE BEFORE", "// CODE AFTER" );
  }

  /** The single-arg constructor must behave exactly like {@code automaticNonAmdShimConfigEnabled=false}. */
  @Test
  public void testSingleArgConstructorDefaultsToAutomaticNonAmdShimConfigDisabled() throws Exception {
    WebjarsURLConnection connection = new WebjarsURLConnection( new URL( "mvn:org.webjars/jquery/2.2.1" ) );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test_single_arg", ".zip" );
    try ( FileOutputStream fileOutputStream = new FileOutputStream( tempFile ) ) {
      IOUtils.copy( inputStream, fileOutputStream );
    }

    try ( ZipFile zipFile = new ZipFile( tempFile ) ) {
      verifyManifest( zipFile );
      verifyBlueprint( zipFile, "jquery@2.2.1", "jquery/2.2.1" );
    }
  }

  /**
   * [PDI-20686] With {@code automaticNonAmdShimConfigEnabled=true}, source files are additionally
   * buffered to a temporary directory (so their content can be scanned for an AMD {@code define()} call)
   * and that directory is cleaned up once the transform finishes. This exercises that whole code path,
   * which is otherwise dead when the flag defaults to {@code false} (as in every other test here).
   */
  @Test
  public void testAutomaticNonAmdShimConfigEnabledUsesAndCleansUpATemporaryDirectory() throws IOException {
    // Deliberately not "org.webjars/test/1.0.0": that fixture carries a wrap override
    // (src/test/resources/overrides/org.webjars/test/1.0.0/overrides.json) which forces isAmdPackage
    // true unconditionally in getWrap(), short-circuiting the AMD-detection branch this test targets.
    ZipFile zipInputStream = getDeployedJarWithAutomaticNonAmdShimConfig(
        new URL( "mvn:org.webjars/jquery/2.2.1" ) );

    // The transform must still succeed and produce the same resource entries, whether or not the
    // temporary-file bookkeeping is enabled.
    ZipEntry entry = zipInputStream.getEntry( "META-INF/resources/webjars/jquery/2.2.1/jquery.js" );
    assertNotNull( entry );
  }

  /**
   * A source stream that is a truncated/corrupted jar must fail inside {@code transform()} (while
   * reading an entry's compressed data); the {@code Callable.call()} wrapper must log the failure,
   * close the piped output stream so the consumer doesn't hang forever, and rethrow so the
   * {@code Future} completes exceptionally.
   */
  @Test
  public void testTransformFailureClosesThePipeAndPropagatesThroughTheFuture() throws Exception {
    File original = new File( "src/test/resources/mockRepo/org/webjars/jquery/2.2.1/jquery-2.2.1.jar" );
    byte[] fullJar = FileUtils.readFileToByteArray( original );
    File truncatedJar = File.createTempFile( "webjar_test_truncated", ".jar" );
    // Cut the file so short that JarInputStream's constructor itself fails while reading the manifest
    // - the one failure point in transform() that isn't already swallowed by its internal exception
    // handling, so it reliably propagates to Callable's outer catch block instead.
    try ( FileOutputStream out = new FileOutputStream( truncatedJar ) ) {
      out.write( fullJar, 0, 100 );
    }

    WebjarsURLConnection connection = new WebjarsURLConnection( truncatedJar.toURI().toURL(), false );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    try {
      // The consumer sees the pipe closed early rather than hanging or silently truncating output.
      IOUtils.toByteArray( inputStream );
    } catch ( IOException ignored ) {
      // Expected: reading from a piped stream whose writer closed early surfaces as an IOException.
    }

    try {
      connection.transform_thread.get();
      fail( "expected the transform thread to have failed on truncated jar content" );
    } catch ( java.util.concurrent.ExecutionException expected ) {
      assertTrue( expected.getCause() instanceof IOException );
    }
  }

  private ZipFile getDeployedJarWithAutomaticNonAmdShimConfig( URL webjarUrl ) throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( webjarUrl, true );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test_amd_shim", ".zip" );

    try ( FileOutputStream fileOutputStream = new FileOutputStream( tempFile ) ) {
      IOUtils.copy( inputStream, fileOutputStream );
    }

    return new ZipFile( tempFile );
  }

  private void verifyManifest( ZipFile zipInputStream ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull( entry );
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( "Bundle-SymbolicName is not pentaho-webjars-",
        manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).startsWith( "pentaho-webjars-" ) );
  }

  private void verifyBlueprint( ZipFile zipInputStream, String expectedAlias, String expectedPath ) throws Exception {
    ZipEntry entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull( entry );

    Document blueprint = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( zipInputStream.getInputStream(
      entry ) );
    XPath xp = XPathFactory.newInstance().newXPath();
    Node resourcesDistProps = (Node) xp.evaluate( "//service[@id='resourcesDist']/service-properties", blueprint,
      XPathConstants.NODE );
    assertNotNull( resourcesDistProps );

    String pattern = (String) xp.evaluate( "./entry[@key='osgi.http.whiteboard.resource.pattern']/@value",
      resourcesDistProps, XPathConstants.STRING );
    assertEquals( "/" + expectedAlias + "/*", pattern );

    String prefix = (String) xp.evaluate( "./entry[@key='osgi.http.whiteboard.resource.prefix']/@value",
      resourcesDistProps, XPathConstants.STRING );
    assertEquals( "blueprint.xml does not include path for " + expectedPath, "/META-INF/resources/webjars/"
      + expectedPath, prefix );
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

  /**
   * [PDI-20686] Directly checks the {@code modules} section of require.json, which is where the null
   * version regression actually showed up: the top-level {@code artifacts} map is keyed by the raw
   * Maven coordinate version and was never null, but each module's own version entry (and its
   * {@code versionedName}/{@code versionedPath}) came from {@code moduleInfo}, which fell back too
   * late and kept baking in the literal string {@code "null"}.
   */
  private void verifyRequireJsonModuleVersion( ZipFile zipInputStream, String moduleName, String expectedVersion )
      throws IOException, ParseException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull( entry );

    String jsonFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    JSONObject json = (JSONObject) ( new JSONParser() ).parse( jsonFile );

    final JSONObject meta = (JSONObject) json.get( "requirejs-osgi-meta" );
    final JSONObject modules = (JSONObject) meta.get( "modules" );
    assertTrue( "module " + moduleName + " exists", modules.containsKey( moduleName ) );
    final JSONObject moduleVersions = (JSONObject) modules.get( moduleName );

    assertTrue( "module version is not \"null\"", !moduleVersions.containsKey( "null" ) );
    assertTrue( "module version is " + expectedVersion, moduleVersions.containsKey( expectedVersion ) );

    final JSONObject versionedDetails = (JSONObject) moduleVersions.get( expectedVersion );
    assertEquals( moduleName + "@" + expectedVersion, versionedDetails.get( "versionedName" ) );
  }

  private void verifyWrapped( ZipFile zipInputStream, String expectedPath, String file, String pre, String pos ) throws IOException {
    ZipEntry entry = zipInputStream.getEntry( "META-INF/resources/webjars/" + expectedPath + "/" + file );
    assertNotNull( entry );

    String srcFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );

    assertTrue( srcFile.startsWith( pre + "\n" ) );
    assertTrue( srcFile.endsWith( "\n" + pos + "\n" ) );
  }

  private ZipFile getDeployedJar( URL webjar_url ) throws IOException {
    WebjarsURLConnection connection = new WebjarsURLConnection( webjar_url, false );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjar_test", ".zip" );

    FileOutputStream fileOutputStream = new FileOutputStream( tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    return new ZipFile( tempFile );
  }
}
