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
