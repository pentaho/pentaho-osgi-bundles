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
 * Copyright 2016 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.i18n.webservice;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.i18n.IPentahoWebPackageLocalizationService;
import org.pentaho.osgi.i18n.LocalizationService;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalizationWebserviceTest {
  private IPentahoWebPackageLocalizationService localizationService;
  private LocalizationWebservice localizationWebservice;

  @Before
  public void setup() {
    localizationService = mock( IPentahoWebPackageLocalizationService.class );
    localizationWebservice = new LocalizationWebservice();
    localizationWebservice.setLocalizationService( localizationService );
  }

  @Test
  public void testWebserviceMethodDefault() {
    testWebServiceMethod( "" );
  }

  @Test
  public void testWebserviceMethodNullLocale() {
    testWebServiceMethod( null );
  }

  @Test
  public void testWebserviceMethodOneLocaleParam() {
    testWebServiceMethod( "en" );
  }

  @Test
  public void testWebserviceMethodTwoLocaleParams() {
    testWebServiceMethod( "en_US" );
  }

  private void testWebServiceMethod( String localeString ) {
    String browserKey = "test.name";
    String serviceKey = "test.name";

    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( localizationService
        .getResourceBundle( eq( serviceKey ), eq( localeString ) ) )
        .thenReturn( resourceBundle );
    assertEquals( resourceBundle, localizationWebservice.getResourceBundle( browserKey, localeString ) );
  }

}
