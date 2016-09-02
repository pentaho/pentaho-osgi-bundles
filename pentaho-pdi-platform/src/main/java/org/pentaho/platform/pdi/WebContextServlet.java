/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
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
 */

package org.pentaho.platform.pdi;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.i18n.LanguageChoice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by nbaker on 7/27/16.
 */
public class WebContextServlet extends HttpServlet {


  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  @Override protected void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
      throws ServletException, IOException {

    // TODO: inject the the css/themes via blueprint

    String requestStr = httpRequest.getRequestURI();
    if ( requestStr != null && requestStr.contains( WEB_CONTEXT_JS ) ) {

      httpResponse.setContentType( "text/javascript" ); //$NON-NLS-1$
      String contextPath = "/";
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append( "var CONTEXT_PATH = '" + contextPath + "';var dojoConfig = [];\n\n" );

      Locale effectiveLocale = getLocale();
      if ( StringUtils.isNotEmpty( httpRequest.getParameter( "locale" ) ) ) {
        effectiveLocale = new Locale( httpRequest.getParameter( "locale" ) );
      }
      appendLocale( stringBuilder, effectiveLocale );

      stringBuilder.
          append( "document.write(\"<link rel='stylesheet' type='text/css' "
              + "href='/content/common-ui/resources/themes/crystal/globalCrystal.css'>\");\n" ).
          append( "document.write(\"<link rel='stylesheet' type='text/css' "
              + "href='../analyzer/styles/themes/crystal/anaCrystal.css'>\");\n" );
      String requireJsLocation = "requirejs-manager/js/require-init.js";
      stringBuilder.append(
          "document.write(\"<script type='text/javascript' src='" + contextPath
              + requireJsLocation + "'></scr\"+\"ipt>\");\n" );

      httpResponse.getWriter().write( stringBuilder.toString() );
    } else {
      httpResponse.sendError( 404 );
    }

  }

  Locale getLocale() {
    // set the locale from PDI
    Locale defaultLocale = LanguageChoice.getInstance().getDefaultLocale();
    if ( defaultLocale == null ) {
      defaultLocale = Locale.getDefault();
    }
    return defaultLocale;
  }

  void appendLocale( StringBuilder sb, Locale locale ) {
    sb.append(
      "var SESSION_LOCALE = '" + locale.toString() + "';\n" ) // Global variable
      // If RequireJs is available, supply a module
      .append(
        "if(typeof(pen) != 'undefined' && pen.define){pen.define('Locale', {locale:'"
          + locale.toString() + "'})};\n" );
  }

  @Override public void destroy() {

  }
}
