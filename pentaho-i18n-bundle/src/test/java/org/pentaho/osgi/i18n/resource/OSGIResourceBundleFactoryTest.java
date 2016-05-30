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
