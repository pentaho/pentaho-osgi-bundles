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
