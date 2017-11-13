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

import org.osgi.framework.Bundle;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.webpackage.core.PentahoWebPackageService;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.ResourceBundle;

@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class LocalizationWebservice implements LocalizationService {


  private LocalizationService localizationService;

  private PentahoWebPackageService webPackageService;

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  public void setWebPackageService( PentahoWebPackageService webPackageService ) {
    this.webPackageService = webPackageService;
  }

  @Override
  public ResourceBundle getResourceBundle( Class clazz, String key, Locale locale ) {
    // Assuming that Java resources are always inside the folder 'i18n'
    String absoluteKey = "/i18n/" + key.replaceAll( "\\.", "/" );

    return ResourceBundle.getBundle( absoluteKey, locale, clazz.getClassLoader() );
  }

  @Override
  public ResourceBundle getResourceBundle( Bundle bundle, String key, Locale locale ) {
    return this.localizationService.getResourceBundle( bundle, key, locale );
  }

  @GET
  @Path( "/{moduleID}" )
  public ResourceBundle getResourceBundleService( @PathParam( "moduleID" ) String moduleID,
                                                  @QueryParam( "locale" ) String localeString ) {
    return null; // this.pentahoWebPackageLocalizationService.getResource( moduleID, locale );
  }

}
