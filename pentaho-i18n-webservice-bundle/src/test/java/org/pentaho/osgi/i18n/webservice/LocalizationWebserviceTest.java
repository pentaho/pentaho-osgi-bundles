/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.osgi.i18n.webservice;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.i18n.LocalizationService;

import java.util.Arrays;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    String browserKey = "test.name";
    String serviceKey = "test.name";
    String localeString = "";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( serviceKey ), eq( Locale.getDefault() ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( browserKey, localeString ) );
  }

  @Test
  public void testWebserviceMethodNullLocale() {
    String browserKey = "test.name";
    String serviceKey = "test.name";
    String localeString = null;
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( serviceKey ), eq( Locale.getDefault() ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( browserKey, localeString ) );
  }

  @Test
  public void testWebserviceMethodOneLocaleParam() {
    String browserKey = "test.name";
    String serviceKey = "test.name";
    String localeString = "en";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
      .getResourceBundle( eq( serviceKey ), eq( new Locale( "en" ) ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( browserKey, localeString ) );
  }

  @Test
  public void testWebserviceMethodTwoLocaleParams() {
    String browserKey = "test.name";
    String serviceKey = "test.name";
    String localeString = "en_US";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService.getResourceBundle( eq( serviceKey ), eq( new Locale( "en", "US" ) ) ) )
      .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundleService( browserKey, localeString ) );
  }

  @Test
  public void testWebserviceWildcard() {
    final String propKey1 = "prop-1";
    final String propValue1 = "value-1";
    final String propKey2 = "prop-2";
    final String propValue2 = "value-2";
    final String keyRegex1 = "testKey1";
    String localeString = "en_US";
    final ResourceBundle resourceBundle1 = new ListResourceBundle() {
      @Override protected Object[][] getContents() {
        return new Object[][]{
          { propKey1, propValue1 }
        };
      }
    };
    final ResourceBundle resourceBundle2 = new ListResourceBundle() {
      @Override protected Object[][] getContents() {
        return new Object[][]{
          { propKey2, propValue2 }
        };
      }
    };

    ResourceBundleRequest resourceBundleRequest = new ResourceBundleRequest();
    resourceBundleRequest.setLocale( localeString );
    ResourceBundleWildcard resourceBundleWildcard1 = new ResourceBundleWildcard();
    resourceBundleWildcard1.setKeyRegex( keyRegex1 );
    resourceBundleRequest.setWildcards( Arrays.asList(resourceBundleWildcard1) );
    when( localizationService.getResourceBundles( any( Pattern.class ), any( Locale.class ) ) ).thenAnswer(
              new Answer<Object>() {
        @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
          Pattern keyPattern = (Pattern) invocation.getArguments()[0];
          assertTrue( keyPattern.matcher( keyRegex1 ).matches() );
          Locale locale = (Locale) invocation.getArguments()[1];
          assertEquals( "en_US", locale.toString() );
          return Arrays.asList( resourceBundle1 );
        }
      } );
    ResourceBundle resourceBundle = localizationWebservice.getResourceBundle( resourceBundleRequest );
    assertEquals( propValue1, resourceBundle.getString( propKey1 ) );
  }
}
