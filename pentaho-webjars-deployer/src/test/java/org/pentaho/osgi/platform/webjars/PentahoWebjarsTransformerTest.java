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
