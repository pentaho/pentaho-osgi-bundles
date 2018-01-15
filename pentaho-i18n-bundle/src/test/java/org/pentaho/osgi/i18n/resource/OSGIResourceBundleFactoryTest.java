/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
