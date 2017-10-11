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

package org.pentaho.i18n.example;

import org.pentaho.osgi.i18n.LocalizationService;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by nbaker on 5/4/16.
 */
@Produces( { MediaType.TEXT_PLAIN } )
@WebService
public class ExampleService {

  private LocalizationService localizationService;

  public ExampleService() {

  }

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  @GET
  @Path( "/" )
  public String getTranslation( @QueryParam( "key" ) String key, @QueryParam( "locale" ) String locale,
                                @QueryParam( "name" ) String name, @Context HttpServletRequest request ) {
    ResourceBundle resourceBundle = localizationService.getResourceBundle( key, new Locale( locale ) );

    if ( resourceBundle == null ) {
      return "Bundle not found";
    }
    if ( !resourceBundle.containsKey( name ) ) {
      return "Key not found";
    }
    return resourceBundle.getString( name );
  }
}
