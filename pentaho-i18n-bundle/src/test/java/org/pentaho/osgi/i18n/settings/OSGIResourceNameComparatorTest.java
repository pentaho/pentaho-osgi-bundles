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
