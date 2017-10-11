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
 * Copyright 2017 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.webpackage.deployer.archive.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlTransformerTest {
  UrlTransformer transformer;

  @Before
  public void setUp() throws Exception {
    this.transformer = new UrlTransformer();
  }

  @Test
  public void canHandleTgzFileWithPackageJson() throws Exception {
    assertTrue( this.transformer.canHandle( getResourceFile( "/my-simple-module-1.4.0.tgz" ) ) );
  }

  @Test
  public void canHandleZipFileWithPackageJson() throws Exception {
    assertTrue( this.transformer.canHandle( getResourceFile( "/my-simple-module-1.4.0.zip" ) ) );
  }

  @Test
  public void canHandleJarFileWithPackageJsonButNoManifest() throws Exception {
    assertTrue( this.transformer.canHandle( getResourceFile( "/my-simple-module-1.4.0.jar" ) ) );
  }

  @Test
  public void cannotHandleTgzFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/no-package-json.tgz" ) ) );
  }

  @Test
  public void cannotHandleZipFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/no-package-json.zip" ) ) );
  }

  @Test
  public void cannotHandleJarFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/no-package-json.jar" ) ) );
  }

  @Test
  public void cannotHandleJarFileWithPackageJsonButWithManifest() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/with-manifest.jar" ) ) );
  }

  @Test
  public void cannotHandleInexistentFile() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "/not-there.zip" ) ) );
  }

  @Test
  public void cannotHandleOtherFiles() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/package.json" ) ) );
  }

  @Test
  public void cannotHandleInvalidTgzFile() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/invalid.tgz" ) ) );
  }

  @Test
  public void cannotHandleInvalidZipFile() throws Exception {
    assertFalse( this.transformer.canHandle( getResourceFile( "/invalid.zip" ) ) );
  }

  File getResourceFile( String path ) {
    return new File( UrlTransformerTest.class.getResource( path ).getFile() );
  }

  /*
   There's no proper way of testing the transform() method, as both ways of adding custom URL protocols
   mess up with global variables, inclusively permanently, so other unit tests would be contaminated.

   URL class is final, so it can't be mocked / extended.

   Calling URL.setURLStreamHandlerFactory changes the behaviour globally and can't even be reverted,
   as URL.setURLStreamHandlerFactory can only be called once (it throws in subsequent calls).

   Changing "java.protocol.handler.pkgs" implies refactoring to comply with the expected naming scheme,
   and it still changes a global property.

   The method is so simple (it just prepends a string to another) that refactoring it to allow testing
   would be an overkill.

  public void transform() throws Exception {
    URL.setURLStreamHandlerFactory( protocol -> {
      if ( protocol.equals( WebPackageURLConnection.URL_PROTOCOL ) ) {
        return new UrlHandler();
      }

      return null;
    } );

    URL url = this.transformer.transform( new URL( "file:/something/foo.jar" ) );
    assertEquals( WebPackageURLConnection.URL_PROTOCOL + ":file:/something/foo.jar", url.toString() );
  }
   */
}
