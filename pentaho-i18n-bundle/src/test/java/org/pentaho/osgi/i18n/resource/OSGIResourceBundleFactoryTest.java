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
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleFactoryTest {
  @Test
  public void testGetBundle() throws IOException {
    String path = "i18n/resource/OSGIResourceBundleTest.properties";
    OSGIResourceBundleFactory osgiResourceBundleFactory =
      new OSGIResourceBundleFactory( path, path, getClass().getClassLoader().getResource(
        path ), 10 );
    assertEquals( path, osgiResourceBundleFactory.getPropertyFilePath() );
    assertEquals( "testValue", osgiResourceBundleFactory.getBundle( null ).getString( "key" ) );
    assertEquals( 10, osgiResourceBundleFactory.getPriority() );
  }

  @Test
  public void testGetBundleReturnsSameWithSameParent() throws IOException {
    String path = "i18n/resource/OSGIResourceBundleTest.properties";
    ResourceBundle parent = mock( ResourceBundle.class );
    OSGIResourceBundleFactory osgiResourceBundleFactory =
      new OSGIResourceBundleFactory( path, path, getClass().getClassLoader().getResource(
        path ), 10 );
    assertTrue( osgiResourceBundleFactory.getBundle( parent ) == osgiResourceBundleFactory.getBundle( parent ) );
  }

  @Test
  public void testGetBundleReturnsDifferentWithDifferentParent() throws IOException {
    String path = "i18n/resource/OSGIResourceBundleTest.properties";
    ResourceBundle parent = mock( ResourceBundle.class );
    ResourceBundle parent2 = mock( ResourceBundle.class );
    OSGIResourceBundleFactory osgiResourceBundleFactory =
      new OSGIResourceBundleFactory( path, path, getClass().getClassLoader().getResource(
        path ), 10 );
    assertTrue( osgiResourceBundleFactory.getBundle( parent ) != osgiResourceBundleFactory.getBundle( parent2 ) );
  }
}
