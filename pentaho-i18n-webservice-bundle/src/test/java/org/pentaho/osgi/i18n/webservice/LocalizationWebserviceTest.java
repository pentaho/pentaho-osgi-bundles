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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
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
import static org.mockito.Matchers.any;
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

  @Test
  public void testWebserviceWildcard() {
    final String propKey1 = "prop-1";
    final String propValue1 = "value-1";
    final String propKey2 = "prop-2";
    final String propValue2 = "value-2";
    final String keyRegex1 = "testKey1";
    final String keyRegex2 = "testKey2";
    final String nameRegex1 = "testName1";
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
    resourceBundleWildcard1.setNameRegex( nameRegex1 );
    ResourceBundleWildcard resourceBundleWildcard2 = new ResourceBundleWildcard();
    resourceBundleWildcard2.setKeyRegex( keyRegex2 );
    resourceBundleRequest.setWildcards( Arrays.asList(resourceBundleWildcard1, resourceBundleWildcard2) );
    when( localizationService.getResourceBundles( any( Pattern.class ), any( Pattern.class ), any( Locale.class ) ) ).thenAnswer(
      new Answer<Object>() {
        @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
          Pattern keyPattern = (Pattern) invocation.getArguments()[0];
          assertTrue( keyPattern.matcher( keyRegex1 ).matches() );
          Pattern namePattern = (Pattern) invocation.getArguments()[1];
          assertTrue( namePattern.matcher( nameRegex1 ).matches() );
          Locale locale = (Locale) invocation.getArguments()[2];
          assertEquals( "en_us", locale.getLanguage() );
          return Arrays.asList( resourceBundle1 );
        }
      } );
    when( localizationService.getResourceBundles( any( Pattern.class ), any( Locale.class ) ) ).thenAnswer(
      new Answer<Object>() {
        @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
          Pattern keyPattern = (Pattern) invocation.getArguments()[ 0 ];
          assertTrue( keyPattern.matcher( keyRegex2 ).matches() );
          Locale locale = (Locale) invocation.getArguments()[ 1 ];
          assertEquals( "en_us", locale.getLanguage() );
          return Arrays.asList( resourceBundle2 );
        }
      } );
    ResourceBundle resourceBundle = localizationWebservice.getResourceBundle( resourceBundleRequest );
    assertEquals( propValue1, resourceBundle.getString( propKey1 ) );
    assertEquals( propValue2, resourceBundle.getString( propKey2 ) );
  }
}
