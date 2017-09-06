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
 * Copyright 2014-2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PentahoWebjarsTransformerTest {
  @Test
  public void testCanHandle() throws Exception {
    PentahoWebjarsTransformer trans = new PentahoWebjarsTransformer();
    assertTrue( trans.canHandle( new File( "src/test/resources/testInput.jar" ) ) );
    assertFalse( trans.canHandle( new File( "src/main/resources/blueprint-template.xml" ) ) );
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

  public void testTransform() throws Exception {
    PentahoWebjarsTransformer trans = new PentahoWebjarsTransformer();
    URL.setURLStreamHandlerFactory( new URLStreamHandlerFactory() {
      @Override public URLStreamHandler createURLStreamHandler( String protocol ) {
        if("pentaho-webjars".equals(protocol)){
          return new WebjarsUrlHandler( true );
        }
        return null;
      }
    } );
    URL url = trans.transform( new URL( "file:/Users/nbaker/foo.jar" ) );
    assertEquals("pentaho-webjars:file:/Users/nbaker/foo.jar", url.toString());
  }
   */
}
