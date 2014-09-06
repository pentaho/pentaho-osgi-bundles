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

package org.pentaho.osgi.i18n.webservice;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.i18n.LocalizationService;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/6/14.
 */
public class LocalizationWebserviceTest {
  private LocalizationService localizationService;
  private LocalizationWebservice localizationWebservice;

  @Before
  public void setup() {
    localizationService = mock( LocalizationService.class );
    localizationWebservice = new LocalizationWebservice();
    localizationWebservice.setLocalizationService( localizationService );
  }

  @Test
  public void testWebserviceMethodDefault() {
    String key = "test-key";
    String name = "test.name";
    String localeString = "";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( key ), eq( name.replaceAll( "\\.", "/" ) ), eq( Locale.getDefault() ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( key, name, localeString ) );
  }

  @Test
  public void testWebserviceMethodOneLocaleParam() {
    String key = "test-key";
    String name = "test.name";
    String localeString = "en";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( key ), eq( name.replaceAll( "\\.", "/" ) ), eq( new Locale( "en" ) ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( key, name, localeString ) );
  }

  @Test
  public void testWebserviceMethodTwoLocaleParams() {
    String key = "test-key";
    String name = "test.name";
    String localeString = "en-US";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( key ), eq( name.replaceAll( "\\.", "/" ) ), eq( new Locale( "en", "US" ) ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( key, name, localeString ) );
  }
}
