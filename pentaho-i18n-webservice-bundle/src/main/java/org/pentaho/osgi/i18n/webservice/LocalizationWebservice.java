/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.i18n.webservice;

import org.pentaho.osgi.i18n.LocalizationService;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/5/14.
 */
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class LocalizationWebservice implements LocalizationService {
  private LocalizationService localizationService;

  @Override public ResourceBundle getResourceBundle( String name, Locale locale ) {
    return localizationService.getResourceBundle( name, locale );
  }

  @Override public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    return localizationService.getResourceBundles( keyRegex, locale );
  }

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  @GET
  @Path( "/{key}/{language}" )
  public ResourceBundle getResourceBundleService( @PathParam( "key" ) String key,
                                                  @PathParam( "language" ) String localeString ) {
    return getResourceBundle( key, getLocale( localeString ) );
  }

  private static Locale getLocale( String localeString ) {
    String[] splitLocale;
    if ( localeString == null || localeString.trim().length() == 0 ) {
      splitLocale = new String[] { };
    } else {
      splitLocale = localeString.split( "_" );
    }
    Locale locale;
    if ( splitLocale.length == 1 ) {
      locale = new Locale( splitLocale[ 0 ] );
    } else if ( splitLocale.length >= 2 ) {
      locale = new Locale( splitLocale[ 0 ], splitLocale[ 1 ] );
    } else {
      locale = Locale.getDefault();
    }
    return locale;
  }

  @POST
  @Path( "/wildcard" )
  public ResourceBundle getResourceBundle( ResourceBundleRequest resourceBundleRequest ) {
    final List<ResourceBundle> resourceBundles = new ArrayList<ResourceBundle>(  );
    for ( ResourceBundleWildcard resourceBundleWildcard : resourceBundleRequest.getWildcards() ) {
      Pattern keyPattern = Pattern.compile( resourceBundleWildcard.getKeyRegex() );
      resourceBundles.addAll( getResourceBundles( keyPattern, getLocale( resourceBundleRequest.getLocale() ) ) );
    }
    return new ListResourceBundle() {
      @Override protected Object[][] getContents() {
        List<Object[]> entries = new ArrayList<Object[]>();
        for ( ResourceBundle resourceBundle : resourceBundles ) {
          for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
            entries.add( new Object[] { key, resourceBundle.getString( key ) } );
          }
        }
        return entries.toArray( new Object[ entries.size() ][] );
      }
    };
  }
}
