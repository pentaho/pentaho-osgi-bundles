/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
    OSGIResourceBundle osgiResourceBundle = new OSGIResourceBundle(
      getClass().getClassLoader().getResource( "org/pentaho/osgi/resource/OSGIResourceBundleTest.properties" ) );
    assertEquals( "testValue", osgiResourceBundle.getString( "key" ) );
    assertEquals( "testValueParent", osgiResourceBundle.getString( "parentKey" ) );
  }

  @Test
  public void testParent() throws IOException {
    OSGIResourceBundle osgiResourceBundle = new OSGIResourceBundle(
      getClass().getClassLoader().getResource( "org/pentaho/osgi/resource/OSGIResourceBundleTest.properties" ) );
    OSGIResourceBundle osgiResourceBundleChild = new OSGIResourceBundle( osgiResourceBundle,
      getClass().getClassLoader().getResource( "org/pentaho/osgi/resource/OSGIResourceBundleTestChild.properties" ) );
    assertEquals( "testValueChild", osgiResourceBundleChild.getString( "key" ) );
    assertEquals( "testValueParent", osgiResourceBundleChild.getString( "parentKey" ) );
  }
}
