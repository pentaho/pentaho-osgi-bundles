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

package org.pentaho.osgi.i18n.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.i18n.IPentahoWebPackageLocalizationService;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceBundleEndpointTest {
  private IPentahoWebPackageLocalizationService localizationService;
  private ResourceBundleEndpoint endpoint;

  @Before
  public void setup() {
    this.localizationService = mock( IPentahoWebPackageLocalizationService.class );

    this.endpoint = new ResourceBundleEndpoint();
    this.endpoint.setLocalizationService( this.localizationService );
  }

  @Test
  public void testGetResourceBundleNullResource() {
    String moduleID = "foobar";

    Response actual   = this.endpoint.getResourceBundle( moduleID, null );
    assertEquals( Status.fromStatusCode( actual.getStatus() ), Status.NOT_FOUND );
  }

  @Test
  public void testGetResourceBundleNoLocale() {
    String moduleID = "foobar_1";
    String locale = "";

    registerResourceBundle( moduleID, locale );
    Response response = this.endpoint.getResourceBundle( moduleID, locale );
    ResourceBundle actualResource = (ResourceBundle) response.getEntity();

    assertEquals( getExpectedLocale( locale ), actualResource.getLocale() );
    assertEquals( moduleID, actualResource.getBaseBundleName() );
  }

  @Test
  public void testGetResourceBundleLanguageOnlyLocale() {
    String moduleID = "foobar_2";
    String locale   = "en";

    registerResourceBundle( moduleID, locale );
    Response response = this.endpoint.getResourceBundle( moduleID, locale );
    ResourceBundle actualResource = (ResourceBundle) response.getEntity();

    assertEquals( getExpectedLocale( locale ), actualResource.getLocale() );
    assertEquals( moduleID, actualResource.getBaseBundleName() );
  }

  @Test
  public void testGetResourceBundleLanguageAndCountryLocale() {
    String moduleID = "foobar_3";
    String locale   = "en-US";

    registerResourceBundle( moduleID, locale );
    Response response = this.endpoint.getResourceBundle( moduleID, locale );
    ResourceBundle actualResource = (ResourceBundle) response.getEntity();

    assertEquals( getExpectedLocale( locale ), actualResource.getLocale() );
    assertEquals( moduleID, actualResource.getBaseBundleName() );
  }

  private void registerResourceBundle( String moduleID, String locale ) {
    String localeString = locale != null ? locale : "";

    ResourceBundle resourceBundle = mock( ResourceBundle.class );
    when( resourceBundle.getLocale() ).thenReturn( Locale.forLanguageTag( localeString ) );
    when( resourceBundle.getBaseBundleName() ).thenReturn( moduleID );

    when( this.localizationService.getResourceBundle( eq( moduleID ), eq( locale ) ) )
        .thenReturn( resourceBundle );
  }

  private Locale getExpectedLocale( String locale ) {
    return Locale.forLanguageTag( locale != null ? locale : "" );
  }

}
