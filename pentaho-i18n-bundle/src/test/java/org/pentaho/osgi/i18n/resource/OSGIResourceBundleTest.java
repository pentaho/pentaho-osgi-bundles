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

package org.pentaho.osgi.i18n.resource;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleTest {
  @Test
  public void testNoParent() throws IOException {
    String path = "i18n/resource/OSGIResourceBundleTest";
    String name = path + ".properties";
    OSGIResourceBundle osgiResourceBundle = new OSGIResourceBundle( path,
      getClass().getClassLoader().getResource( name ) );
    assertEquals( "testValue", osgiResourceBundle.getString( "key" ) );
    assertEquals( "testValueParent", osgiResourceBundle.getString( "parentKey" ) );
    assertEquals( path, osgiResourceBundle.getDefaultName() );
  }

  @Test
  public void testParent() throws IOException {
    String parentPath = "i18n/resource/OSGIResourceBundleTest.properties";
    OSGIResourceBundle osgiResourceBundle = new OSGIResourceBundle( parentPath,
      getClass().getClassLoader().getResource( parentPath ) );
    String childPath = "i18n/resource/OSGIResourceBundleTestChild.properties";
    OSGIResourceBundle osgiResourceBundleChild = new OSGIResourceBundle( childPath, osgiResourceBundle,
      getClass().getClassLoader().getResource( childPath ) );
    assertEquals( osgiResourceBundle, osgiResourceBundleChild.getParent() );
    assertEquals( "testValueChild", osgiResourceBundleChild.getString( "key" ) );
    assertEquals( "testValueParent", osgiResourceBundleChild.getString( "parentKey" ) );
  }
}
