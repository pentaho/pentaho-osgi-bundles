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

package org.pentaho.osgi.i18n.settings;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Viktoryia_Klimenka on 5/30/2016.
 */
public class OSGIResourceNamingConventionTest {

  @Test( expected = NullPointerException.class )
  public void testNullName() {
    String name = null;
    OSGIResourceNamingConvention.getResourceNameMatcher( name );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testEmptyName() {
    String name = "";
    OSGIResourceNamingConvention.getResourceNameMatcher( name );
  }

  @Test
  public void testValidName() {
    String name = "i18n/messages.properties";
    assertNotNull( OSGIResourceNamingConvention.getResourceNameMatcher( name ) );
    name = "i18n/mesasages_fr.properties";
    assertNotNull( OSGIResourceNamingConvention.getResourceNameMatcher( name ) );
    name = "i18n/bundle/mesasages_fr.properties.3";
    assertNotNull( OSGIResourceNamingConvention.getResourceNameMatcher( name ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testInvalidNameNoFolder() {
    String name = "messages.properties";
    OSGIResourceNamingConvention.getResourceNameMatcher( name );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testInvalidNameDotDelimeter() {
    String name = "i18n.mesasages_fr.properties.3";
    OSGIResourceNamingConvention.getResourceNameMatcher( name );
  }
}
