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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class WebjarsURLConnectionTest {
  @Test
  public void testConnection() throws IOException {

    File input = new File("src/test/resources/testInput.jar");
    assertTrue( "Test jar not found",  input.exists() );
    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );

    connection.connect();
    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webjars", ".zip" ); //new File("src/test/resources/testOutput.jar");
    byte[] buff = new byte[2048];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    ZipFile zipInputStream = new ZipFile( tempFile );

    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull(entry);
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ));

    entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull(entry);
    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/angularjs/1.3.0-rc.0\" />" ));
    entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull(entry);

  }

  @Test
  public void testPomRequireConfig() throws IOException {

    File input = new File("src/test/resources/testPomRequire.jar");
    assertTrue( "Test testPomRequire.jar not found",  input.exists() );

    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "testPomRequire", ".zip" ); //new File("src/test/resources/testOutput.jar");
    byte[] buff = new byte[2048];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    ZipFile zipInputStream = new ZipFile( tempFile );

    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull(entry);
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ));

    entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull(entry);
    
    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/smart-table/2.0.3-1\" />" ));
    
    entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull(entry);

  }

  @Test
  public void testBowerRequireConfig() throws IOException {
    
    File input = new File("src/test/resources/testBowerRequire.jar");
    assertTrue( "Test testBowerRequire.jar not found",  input.exists() );

    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "testBowerRequire", ".zip" ); //new File("src/test/resources/testOutput.zip");
    byte[] buff = new byte[2048];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    ZipFile zipInputStream = new ZipFile( tempFile );

    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull(entry);
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ));

    entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull(entry);
    
    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/angular-ui-router.stateHelper/1.3.1\" />" ));
    
    entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull(entry);

  }

  @Test
  public void testNpmRequireConfig() throws IOException {
    
    File input = new File("src/test/resources/testNpmRequire.jar");
    assertTrue( "Test testNpmRequire.jar not found",  input.exists() );

    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "testBowerRequire", ".zip" ); //new File("src/test/resources/testOutput.zip");
    byte[] buff = new byte[2048];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    ZipFile zipInputStream = new ZipFile( tempFile );

    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull(entry);
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ));

    entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull(entry);
    
    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/asap/2.0.3\" />" ));
    
    entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull(entry);

  }

  @Test
  public void testJustSrcRequireConfig() throws IOException {
    
    File input = new File("src/test/resources/testJustSrc.jar");
    assertTrue( "Test testJustSrc.jar not found",  input.exists() );

    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "testJustSrcRequire", ".zip" ); //new File("src/test/resources/testOutput.zip");
    byte[] buff = new byte[2048];
    int ret;

    FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    // Verify Zip contents
    ZipFile zipInputStream = new ZipFile( tempFile );

    ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
    assertNotNull(entry);
    Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
    assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ));

    entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
    assertNotNull(entry);
    
    String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
    assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/angular-dateparser/1.0.9\" />" ));
    
    entry = zipInputStream.getEntry( "META-INF/js/require.json" );
    assertNotNull(entry);

  }

}