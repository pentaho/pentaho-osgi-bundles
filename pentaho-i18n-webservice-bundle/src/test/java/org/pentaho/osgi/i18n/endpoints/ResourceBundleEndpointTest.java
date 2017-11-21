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
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
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
  public void testGetResourceBundleNotFound() {
    when( this.localizationService.getResourceBundle( anyString(), anyString() ) ).thenReturn( null );

    Response response = this.endpoint.getResourceBundle( "", "" );
    assertNotNull( response );

    Status actualStatus = Status.fromStatusCode( response.getStatus() );
    assertEquals( Status.NOT_FOUND, actualStatus );
  }

  @Test
  public void testGetResourceBundleOK() {
    String moduleID = "foobar_1";
    String locale = "en";
    ResourceBundle resourceBundle = mock( ResourceBundle.class );

    when( this.localizationService.getResourceBundle( eq( moduleID ), eq( locale ) ) ).thenReturn( resourceBundle );
    Response response = this.endpoint.getResourceBundle( moduleID, locale );
    assertNotNull( response );

    Status actualStatus = Status.fromStatusCode( response.getStatus() );
    assertEquals( Status.OK, actualStatus );
  }

}
