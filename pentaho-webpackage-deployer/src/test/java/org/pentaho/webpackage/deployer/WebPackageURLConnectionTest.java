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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.webpackage.deployer;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WebPackageURLConnectionTest {
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testTgzFileWithPackageJson() throws Exception {
    JarFile jarFile = getDeployedJar( new File( "src/test/resources/my-simple-module-1.4.0.tgz" ).toURI().toURL() );

    Manifest manifest = jarFile.getManifest();

    verifyManifest( manifest );
  }

  @Test
  public void testZipFileWithPackageJson() throws Exception {
    JarFile jarFile = getDeployedJar( new File( "src/test/resources/my-simple-module-1.4.0.zip" ).toURI().toURL() );

    Manifest manifest = jarFile.getManifest();

    verifyManifest( manifest );
  }

  @Test
  public void testClosingStream() throws IOException {
    WebPackageURLConnection connection = new WebPackageURLConnection( new File( "src/test/resources/my-simple-module-1.4.0.zip" ).toURI().toURL() );
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
    WebPackageURLConnection connection = new WebPackageURLConnection( new File( "src/test/resources/not-there.zip" ).toURI().toURL() );
    connection.connect();

    connection.getInputStream();
  }

  private void verifyManifest( Manifest manifest ) {
    assertTrue( manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME ).startsWith( "pentaho-web-package-" ) );
    assertTrue( manifest.getMainAttributes().getValue( Constants.PROVIDE_CAPABILITY ).startsWith( "org.pentaho.webpackage;name=\"my-simple-module\";version:Version=\"1.4.0\";root=\"/pwp-" ) );
  }

  private JarFile getDeployedJar( URL url ) throws IOException {
    WebPackageURLConnection connection = new WebPackageURLConnection( url );
    connection.connect();

    InputStream inputStream = connection.getInputStream();
    File tempFile = File.createTempFile( "webpackage_test", ".jar" );

    FileOutputStream fileOutputStream = new FileOutputStream( tempFile );

    IOUtils.copy( inputStream, fileOutputStream );

    return new JarFile( tempFile );
  }
}
