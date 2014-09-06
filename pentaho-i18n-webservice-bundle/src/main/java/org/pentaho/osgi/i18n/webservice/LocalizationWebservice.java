/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.i18n.webservice;

import org.pentaho.osgi.i18n.LocalizationService;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/5/14.
 */
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class LocalizationWebservice implements LocalizationService {
  private LocalizationService localizationService;

  @Override public ResourceBundle getResourceBundle( String key, String name, Locale locale ) {
    return localizationService.getResourceBundle( key, name, locale );
  }

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  @GET
  @Path( "/{key}/{name}/{language}" )
  public ResourceBundle getResourceBundleService( @PathParam( "key" ) String key, @PathParam( "name" ) String name,
                                                  @PathParam( "language" ) String localeString ) {
    name = name.replaceAll( "\\.", "/" );
    String[] splitLocale;
    if ( localeString == null || localeString.trim().length() == 0 ) {
      splitLocale = new String[] { };
    } else {
      splitLocale = localeString.split( "-" );
    }
    Locale locale;
    if ( splitLocale.length == 1 ) {
      locale = new Locale( splitLocale[ 0 ] );
    } else if ( splitLocale.length >= 2 ) {
      locale = new Locale( splitLocale[ 0 ], splitLocale[ 1 ] );
    } else {
      locale = Locale.getDefault();
    }
    return getResourceBundle( key, name, locale );
  }
}
