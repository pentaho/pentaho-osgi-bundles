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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
  public void testGetResourceBundleOnlyDefaultProperties() {
    Locale locale = Locale.forLanguageTag( "" );

    Map<String, String> expectedResult = new HashMap<>( 2 );
    expectedResult.put( "default.key", "default-value" );
    expectedResult.put( "shared.key", "shared-value" );

    assertResourceBundleMessages( locale, expectedResult );
  }

  @Test
  public void testGetResourceBundleDefaultMergedWithLanguageProperties() {
    Locale locale = Locale.forLanguageTag( "de" );

    Map<String, String> expectedResult = new HashMap<>( 3 );
    expectedResult.put( "default.key", "default-value" );
    expectedResult.put( "default.de.key", "default-de-value" );
    expectedResult.put( "shared.key", "de-shared-value" );

    assertResourceBundleMessages( locale, expectedResult );
  }

  @Test
  public void testGetResourceBundleDefaultMergedWithLanguageAndCountryProperties() {
    Locale locale = Locale.forLanguageTag( "de-DE" );

    Map<String, String> expectedResult = new HashMap<>( 4 );
    expectedResult.put( "default.key", "default-value" );
    expectedResult.put( "default.de.key", "default-de-value" );
    expectedResult.put( "default.de.DE.key", "default-de-DE-value" );
    expectedResult.put( "shared.key", "de-DE-shared-value" );

    assertResourceBundleMessages( locale, expectedResult );
  }

  private void assertResourceBundleMessages( Locale locale, Map<String, String> expectedResult ) {
    ResourceBundle bundle = this.localizationService.getResourceBundle( getClass(), RESOURCE_PATH, locale );
    List<String> keys = Collections.list( bundle.getKeys() );

    assertEquals( expectedResult.size(), keys.size() );
    for ( String messageKey : keys ) {
      assertEquals( expectedResult.get( messageKey ), bundle.getString( messageKey ) );
    }

  }
}
