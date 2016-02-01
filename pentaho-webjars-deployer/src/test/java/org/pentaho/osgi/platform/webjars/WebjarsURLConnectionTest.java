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
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class WebjarsURLConnectionTest {

  @Test
  public void testClosingStream() throws IOException {
    File input = new File( "src/test/resources/testInput.jar" );
    assertTrue( "Test jar not found",  input.exists() );
    WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );

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
  public void testConnection() throws IOException {
    ZipFile zipInputStream = null;
    try {
      File input = new File( "src/test/resources/testInput.jar" );
      assertTrue( "Test jar not found",  input.exists() );
      WebjarsURLConnection connection = new WebjarsURLConnection( input.toURI().toURL() );

      connection.connect();
      InputStream inputStream = connection.getInputStream();
      File tempFile = File.createTempFile( "webjars", ".zip" ); //new File("src/test/resources/testOutput.jar");

      FileOutputStream fileOutputStream = new FileOutputStream(  tempFile );

      IOUtils.copy( inputStream, fileOutputStream );

      // Verify Zip contents
      zipInputStream = new ZipFile( tempFile );

      ZipEntry entry = zipInputStream.getEntry( "META-INF/MANIFEST.MF" );
      assertNotNull( entry );
      Manifest manifest = new Manifest( zipInputStream.getInputStream( entry ) );
      assertTrue( manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ).toString().startsWith( "pentaho-webjars-" ) );

      entry = zipInputStream.getEntry( "OSGI-INF/blueprint/blueprint.xml" );
      assertNotNull( entry );
      String bpFile = IOUtils.toString( zipInputStream.getInputStream( entry ), "UTF-8" );
      assertTrue( bpFile.contains( "<property name=\"path\" value=\"/META-INF/resources/webjars/angularjs/1.3.0-rc.0\" />" ) );
      entry = zipInputStream.getEntry( "META-INF/js/require.json" );
      assertNotNull( entry );
    } finally {
      if ( zipInputStream != null ) {
        zipInputStream.close();
      }
    }
  }
}
