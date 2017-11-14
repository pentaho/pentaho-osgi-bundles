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

package org.pentaho.osgi.i18n.webservice.impl;

import org.pentaho.osgi.i18n.webservice.ILocalizationWebservice;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.webpackage.core.PentahoWebPackageResource;
import org.pentaho.webpackage.core.PentahoWebPackageService;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.ResourceBundle;

@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class LocalizationWebservice implements ILocalizationWebservice {
  private LocalizationService localizationService;
  private PentahoWebPackageService webPackageService;

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  public void setWebPackageService( PentahoWebPackageService webPackageService ) {
    this.webPackageService = webPackageService;
  }

  @GET
  @Path( "/" )
  @Override
  public ResourceBundle getResourceBundle( @QueryParam( "moduleID" ) String moduleID,
                                           @QueryParam( "locale" ) String localeString ) {
    PentahoWebPackageResource resource = this.webPackageService.resolve( moduleID );
    Locale locale = getLocale( localeString );

    return this.localizationService.getResourceBundle( resource.getClassLoader(), resource.getResourcePath(), locale );
  }

  private Locale getLocale( String localeString ) {
    String languageTag = localeString != null ? localeString.replace( "_", "-" ) : "";

    return Locale.forLanguageTag( languageTag );
  }
}
