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
