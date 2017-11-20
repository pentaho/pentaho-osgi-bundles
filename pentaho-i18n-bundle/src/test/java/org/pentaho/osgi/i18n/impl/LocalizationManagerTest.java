/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.osgi.i18n.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.i18n.LocalizationService;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LocalizationManagerTest {
  private static final String RESOURCE_PATH = "org/pentaho/osgi/i18n/impl/messages";
  private LocalizationService localizationService;

  @Before
  public void setup() {
    this.localizationService = new LocalizationManager();
  }

  @Test( expected = NullPointerException.class )
  public void testGetResourceBundleNullLocale() {
    this.localizationService.getResourceBundle( getClass(), RESOURCE_PATH, null );
  }

  @Test
  public void testGetResourceBundleDefault() {
    Locale locale = Locale.forLanguageTag( "" );

    ResourceBundle bundle = this.localizationService.getResourceBundle( getClass(), RESOURCE_PATH, locale );
    assertEquals( "messages.properties", bundle.getString( "key" ) );

    try {
      bundle.getString( "key.de" );
      bundle.getString( "key.de.DE" );

      fail( "Should have thrown a MissingResourceException" );
    } catch( MissingResourceException mre ) {
      // ...
    }

  }

  @Test
  public void testGetResourceBundleLanguage() {
    Locale locale = Locale.forLanguageTag( "de" );

    ResourceBundle bundle = this.localizationService.getResourceBundle( getClass(), RESOURCE_PATH, locale );

    assertEquals( "messages_de.properties", bundle.getString( "key" ) );

    assertEquals( "de key", bundle.getString( "key.de" ) );
    try {
      bundle.getString( "key.de.DE" );

      fail( "Should have thrown a MissingResourceException" );
    } catch( MissingResourceException mre ) {
      // ...
    }

  }

  @Test
  public void testGetResourceBundleLanguageAndCountry() {
    Locale locale = Locale.forLanguageTag( "de-DE" );

    ResourceBundle bundle = this.localizationService.getResourceBundle( getClass(), RESOURCE_PATH, locale );
    assertEquals( "messages_de_DE.properties", bundle.getString( "key" ) );

    assertEquals( "de key", bundle.getString( "key.de" ) );
    assertEquals( "de_DE key", bundle.getString( "key.de.DE" ) );
  }
}
