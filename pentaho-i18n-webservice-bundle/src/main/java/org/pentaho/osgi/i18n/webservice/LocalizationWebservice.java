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
import org.pentaho.webpackage.core.PentahoWebPackage;
import org.pentaho.webpackage.core.PentahoWebPackageService;

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

@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
@WebService
public class LocalizationWebservice implements LocalizationService {

  private static int LANGUAGE_ONLY_BUNDLE = 1;
  private static int LANGUAGE_COUNTRY_BUNDLE = 2;

  private LocalizationService localizationService;

  private PentahoWebPackageService webPackageService;

  @Override
  public ResourceBundle getResourceBundle( Class clazz, String key, Locale locale ) {
    return this.localizationService.getResourceBundle( /*clazz, */key, locale );
  }

  @Override
  public ResourceBundle getResourceBundle( Bundle bundle, String key, Locale locale ) {
    return this.localizationService.getResourceBundle( /*bundle, */key, locale );
  }

  @Override
  public ResourceBundle getResourceBundle( String key, Locale locale ) {
    return this.localizationService.getResourceBundle( key, locale );
  }

  @Override
  public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    return this.localizationService.getResourceBundles( keyRegex, locale );
  }

  public void setLocalizationService( LocalizationService localizationService ) {
    this.localizationService = localizationService;
  }

  public void setWebPackageService( PentahoWebPackageService webPackageService ) {
    this.webPackageService = webPackageService;
  }

  @GET
  @Path( "/{context}/{key}/{language}" )
  public ResourceBundle getResourceBundleService( @PathParam( "context" ) String context,
                                                  @PathParam( "key" ) String relativeKey,
                                                  @PathParam( "language" ) String localeString ) {
    // context: det-impl-webclient_8.1-SNAPSHOT
    // key:path.to.bundle* (e.g. "_en.properties")
    // language: en

    PentahoWebPackage webPackage = findWebPackage( context );

    Bundle bundle = webPackage != null ? webPackage.getBundle() : null;
    String absoluteKey = ( webPackage != null ? webPackage.getResourceRootPath() : "/i18n/" )
        + relativeKey.replaceAll( "\\.", "/" );

    Locale locale = getLocale( localeString );

    return getResourceBundle( bundle, absoluteKey, locale );
  }

  @POST
  @Path( "/wildcard" )
  public ResourceBundle getResourceBundle( ResourceBundleRequest resourceBundleRequest ) {
    final List<ResourceBundle> resourceBundles = new ArrayList<>();

    for ( ResourceBundleWildcard resourceBundleWildcard : resourceBundleRequest.getWildcards() ) {
      Pattern keyPattern = Pattern.compile( resourceBundleWildcard.getKeyRegex() );
      Locale locale = getLocale( resourceBundleRequest.getLocale() );

      List<ResourceBundle> resourceBundleList = getResourceBundles( keyPattern, locale );
      resourceBundles.addAll( resourceBundleList );
    }

    return new ListResourceBundle() {
      @Override protected Object[][] getContents() {
        List<Object[]> entries = new ArrayList<>();

        for ( ResourceBundle resourceBundle : resourceBundles ) {
          for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
            String entry = resourceBundle.getString( key );
            entries.add( new Object[] { key, entry } );
          }
        }

        return entries.toArray( new Object[ entries.size() ][] );
      }
    };
  }

  private static Locale getLocale( String localeString ) {
    boolean isValidLocale = localeString != null && !localeString.matches( "^\\s*$" );

    if ( !isValidLocale ) {
      return Locale.getDefault();
    }

    String[] localeParams = localeString.split( "_" );

    int localeType = localeParams.length;
    boolean hasLanguageAndCountry = localeType >= LANGUAGE_COUNTRY_BUNDLE;
    boolean hasLanguage = hasLanguageAndCountry || localeType == LANGUAGE_ONLY_BUNDLE;

    String language = hasLanguage ? localeParams[ 0 ] : "";
    String country = hasLanguageAndCountry ? localeParams[ 1 ] : "";

    return new Locale( language, country );
  }

  private PentahoWebPackage findWebPackage( String context ) {
    String[] contextInfo = context.split( "_" );
    String contextPackageName = contextInfo[0];
    String contextPackageVersion = contextInfo[1];

    return this.webPackageService.findWebPackage( contextPackageName, contextPackageVersion );
  }
}
