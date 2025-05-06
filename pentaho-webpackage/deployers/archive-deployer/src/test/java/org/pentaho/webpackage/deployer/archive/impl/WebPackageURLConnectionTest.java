/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.webpackage.deployer.archive.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.pentaho.webpackage.core.PentahoWebPackageConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
    JarFile jarFile = getDeployedJar( getResourceUrl( "/my-simple-module-1.4.0.tgz" ) );

    Manifest manifest = jarFile.getManifest();

    verifyManifest( manifest, "my-simple-module", "1.4.0" );
  }

  @Test
  public void testZipFileWithPackageJson() throws Exception {
    JarFile jarFile = getDeployedJar( getResourceUrl( "/my-simple-module-1.4.0.zip" ) );

    Manifest manifest = jarFile.getManifest();

    verifyManifest( manifest, "my-simple-module", "1.4.0" );
  }

  @Test
  public void testZipFileWithMacOsMetadata() throws Exception {
    JarFile jarFile = getDeployedJar( getResourceUrl( "/macos.zip" ) );

    Manifest manifest = jarFile.getManifest();

    verifyManifest( manifest, "my-module", "1.4.2" );
  }

  @Test
  public void testClosingStream() throws IOException {
    WebPackageURLConnection connection = new WebPackageURLConnection( getResourceUrl( "/my-simple-module-1.4.0.zip" ) );
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
    WebPackageURLConnection connection = new WebPackageURLConnection( new File( "/not-there.zip" ).toURI().toURL() );
    connection.connect();

    connection.getInputStream();
  }

  private void verifyManifest( Manifest manifest, String moduleName, String moduleVersion ) {
    assertTrue( manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME ).startsWith( "pentaho-webpackage-" ) );
    assertTrue( manifest.getMainAttributes().getValue( Constants.PROVIDE_CAPABILITY ).startsWith( PentahoWebPackageConstants.CAPABILITY_NAMESPACE + ";name=\"" + moduleName + "\";version:Version=\"" + moduleVersion + "\";root=\"/pwp-" ) );
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

  URL getResourceUrl( final String path ) {
    try {
      return WebPackageURLConnectionTest.class.getResource( path ).toURI().toURL();
    } catch ( MalformedURLException | URISyntaxException ignored ) {
    }

    return null;
  }
}
