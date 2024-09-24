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
package org.pentaho.osgi.i18n.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Viktoryia_Klimenka on 6/7/2016.
 */
public class OSGIResourceNameComparatorTest {
  OSGIResourceNameComparator comparator;

  @Before
  public void setup() {
    comparator = new OSGIResourceNameComparator();
  }

  @Test( expected = NullPointerException.class )
  public void testNullName() {
    String first = null;
    String second = "bundle/messages.properties";
    comparator.compare( first, second );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testEmptyName() {
    String first = "";
    String second = "bundle/messages.properties";
    comparator.compare( first, second );
  }

  @Test
  public void testNoPriority() {
    String first = "bundle/messages_en.properties";
    String second = "bundle/messages.properties";
    Assert.assertTrue( comparator.compare( first, second ) > 0 );
  }

  @Test
  public void testWithPriority() {
    String first = "bundle/messages.properties.3";
    String second = "bundle/messages.properties.2";
    Assert.assertTrue( comparator.compare( first, second ) > 0 );
  }

  @Test
  public void testMixed() {
    String first = "bundle/messages.properties.2";
    String second = "bundle/messages.properties";
    Assert.assertTrue( comparator.compare( first, second ) > 0 );
  }

}
