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
    assertTrue( this.transformer.canHandle( new File( "src/test/resources/my-simple-module-1.4.0.tgz" ) ) );
  }

  @Test
  public void canHandleZipFileWithPackageJson() throws Exception {
    assertTrue( this.transformer.canHandle( new File( "src/test/resources/my-simple-module-1.4.0.zip" ) ) );
  }

  @Test
  public void canHandleJarFileWithPackageJsonButNoManifest() throws Exception {
    assertTrue( this.transformer.canHandle( new File( "src/test/resources/my-simple-module-1.4.0.zip" ) ) );
  }

  @Test
  public void cannotHandleTgzFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/no-package-json.tgz" ) ) );
  }

  @Test
  public void cannotHandleZipFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/no-package-json.zip" ) ) );
  }

  @Test
  public void cannotHandleJarFileWithNoPackageJson() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/no-package-json.zip" ) ) );
  }

  @Test
  public void cannotHandleJarFileWithPackageJsonButWithManifest() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/with-manifest.zip" ) ) );
  }

  @Test
  public void cannotHandleInexistentFile() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/not-there.zip" ) ) );
  }

  @Test
  public void cannotHandleOtherFiles() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/package.json" ) ) );
  }

  @Test
  public void cannotHandleInvalidTgzFile() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/invalid.tgz" ) ) );
  }

  @Test
  public void cannotHandleInvalidZipFile() throws Exception {
    assertFalse( this.transformer.canHandle( new File( "src/test/resources/invalid.zip" ) ) );
  }

  @Test
  public void transform() throws Exception {
    // invalid unit test, as it changes permanently the URL's stream handler factory
//    URL.setURLStreamHandlerFactory( protocol -> {
//      if(protocol.equals( "pentaho-web-package" )) {
//        return new UrlHandler();
//      }
//
//      return null;
//    } );
//
//    URL url = this.transformer.transform( new URL( "file:/something/foo.jar" ) );
//    assertEquals("pentaho-web-package:file:/something/foo.jar", url.toString());
  }
}
