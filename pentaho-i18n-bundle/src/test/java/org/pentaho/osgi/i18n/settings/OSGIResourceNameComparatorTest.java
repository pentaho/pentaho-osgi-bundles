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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
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
