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
 * Copyright 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.i18n.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.webpackage.core.PentahoWebPackageResource;
import org.pentaho.webpackage.core.PentahoWebPackageService;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.junit.Assert.assertEquals;

public class PentahoWebPackageLocalizationServiceTest {
  private static final String NAME_VERSIONED = "test_1.0";

  private PentahoWebPackageLocalizationService pwpLocalizationService;
  private ClassLoader loaderMock;

  @Before
  public void setup() {
    this.pwpLocalizationService = new PentahoWebPackageLocalizationService();
    this.loaderMock = mock( ClassLoader.class );
  }

  @Test
  public void testGetResourceBundleNoWebPackageResource() {
    setupGetResourceBundleTest();

    ResourceBundle actualResourceBundle = this.pwpLocalizationService
        .getResourceBundle( getModuleID( "/path/to/resource" ), null );

    assertNull( actualResourceBundle );
  }

  @Test
  public void testGetResourceBundleNoResourceBundle() {
    String resourcePath = "/path/to/resource";
    Locale locale = Locale.getDefault();

    setupGetResourceBundleTest( resourcePath, locale );

    ResourceBundle actualResourceBundle = this.pwpLocalizationService
        .getResourceBundle( getModuleID( "/path/to/other/resource" ), locale.toLanguageTag() );

    assertNull( actualResourceBundle );
  }

  @Test
  public void testGetResourceBundleNoLocale() {
    String resourcePath = "/path/to/resource";
    setupGetResourceBundleTest( resourcePath, null );

    ResourceBundle actualResourceBundle = this.pwpLocalizationService
        .getResourceBundle( getModuleID( resourcePath ), null );

    assertEquals( resourcePath, actualResourceBundle.getBaseBundleName() );
    assertEquals( Locale.forLanguageTag( "" ), actualResourceBundle.getLocale() );
  }

  @Test
  public void testGetResourceBundleWithLocale() {
    String resourcePath = "/path/to/resource2";
    Locale locale = Locale.US;

    setupGetResourceBundleTest( resourcePath, locale );

    ResourceBundle actualResourceBundle = this.pwpLocalizationService
        .getResourceBundle( getModuleID( resourcePath ), locale.toLanguageTag() );

    assertEquals( resourcePath, actualResourceBundle.getBaseBundleName() );
    assertEquals( locale, actualResourceBundle.getLocale() );
  }

  // region Aux Test Methods
  private void setupGetResourceBundleTest() {
    PentahoWebPackageService pwpService = mock( PentahoWebPackageService.class );
    when( pwpService.resolve( anyString() ) ).thenReturn( null );
    this.pwpLocalizationService.setWebPackageService( pwpService );

    LocalizationService localizationService = mock( LocalizationService.class );
    when( localizationService.getResourceBundle( any( ClassLoader.class ), anyString(), any( Locale.class ) ) )
        .thenReturn( null );
    this.pwpLocalizationService.setLocalizationService( localizationService );
  }

  private void setupGetResourceBundleTest( String resourcePath, Locale locale ) {
    // Setup PentahoWebPackageService
    PentahoWebPackageService pwpService = mock( PentahoWebPackageService.class );

    PentahoWebPackageResource resource = mock( PentahoWebPackageResource.class );
    when( resource.getResourcePath() ).thenReturn( resourcePath );
    when( resource.getClassLoader() ).thenReturn( this.loaderMock );

    String moduleID = getModuleID( resourcePath );
    when( pwpService.resolve( eq( moduleID ) ) ).thenReturn( resource );
    this.pwpLocalizationService.setWebPackageService( pwpService );

    // Setup LocalizationService
    LocalizationService localizationService = mock( LocalizationService.class );
    Locale effectiveLocale = locale != null ? locale : Locale.forLanguageTag( "" );

    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( resourceBundle.getBaseBundleName() ).thenReturn( resourcePath );
    when( resourceBundle.getLocale() ).thenReturn( effectiveLocale );

    when( localizationService.getResourceBundle( eq( this.loaderMock ), eq( resourcePath ), eq( effectiveLocale ) ) )
        .thenReturn( resourceBundle );
    this.pwpLocalizationService.setLocalizationService( localizationService );
  }

  private String getModuleID( String resourcePath ) {
    return NAME_VERSIONED + resourcePath;
  }
  // endregion
}
