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

import org.pentaho.osgi.i18n.IPentahoWebPackageLocalizationService;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ResourceBundle;


@Path( "{moduleID: .+}" )
public class ResourceBundleEndpoint {
  private IPentahoWebPackageLocalizationService localizationService;

  public void setLocalizationService( IPentahoWebPackageLocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  @GET
  @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
  public Response getResourceBundle( @PathParam( "moduleID" ) String moduleID,
                                     @QueryParam( "locale" ) @DefaultValue( "" ) String localeString ) {
    ResourceBundle resource = this.localizationService.getResourceBundle( moduleID, localeString );
    if ( resource == null ) {
      return Response.status( Response.Status.NOT_FOUND ).build();
    }

    return Response.ok( resource ).build();
  }
}
