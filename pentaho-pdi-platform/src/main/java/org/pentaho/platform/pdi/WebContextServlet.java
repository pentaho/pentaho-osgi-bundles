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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.platform.api.engine.IPlatformWebResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nbaker on 7/27/16.
 */
public class WebContextServlet extends HttpServlet {
  // this is the map that groups contexts to a set of urls to add to the context
  private Map<String, Set<IPlatformWebResource>> contextResourcesMap = new HashMap<>();

  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  @Override protected void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
      throws ServletException, IOException {

    String requestStr = httpRequest.getRequestURI();
    if ( requestStr != null && requestStr.contains( WEB_CONTEXT_JS ) ) {

      httpResponse.setContentType( "text/javascript" ); //$NON-NLS-1$
      String contextPath = "/";
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append( "var CONTEXT_PATH = '" + contextPath + "';\nvar dojoConfig = [];\n\n" );

      Locale effectiveLocale = getLocale();
      if ( StringUtils.isNotEmpty( httpRequest.getParameter( "locale" ) ) ) {
        effectiveLocale = new Locale( httpRequest.getParameter( "locale" ) );
      }
      appendLocale( stringBuilder, effectiveLocale );

      String context = null;
      if ( StringUtils.isNotEmpty( httpRequest.getParameter( "context" ) ) ) {
        context = httpRequest.getParameter( "context" );
      }

      appendJsWebResources( stringBuilder, getWebResources( context, ".*\\.js" ) );
      appendCssWebResources( stringBuilder, getWebResources( context, ".*\\.css" ) );

      String requireJsLocation = "requirejs-manager/js/require-init.js";
      stringBuilder.append(
          "document.write(\"<script type='text/javascript' src='" + contextPath
              + requireJsLocation + "'></scr\"+\"ipt>\");\n" );

      httpResponse.getWriter().write( stringBuilder.toString() );
    } else {
      httpResponse.sendError( 404 );
    }

  }

  List<String> getWebResources( String context, String filePattern ) {
    Set<IPlatformWebResource> resources = contextResourcesMap.get( context );
    if ( CollectionUtils.isNotEmpty( resources ) ) {
      List<String> webResources = resources.stream()
        .filter( iPlatformWebResource -> iPlatformWebResource.getLocation().matches( filePattern ) )
        .map( IPlatformWebResource::getLocation )
        .collect( Collectors.toList() );
      return webResources;
    }
    return Collections.EMPTY_LIST;
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

  /**
   * Add any resource to the web context
   * @param resource
   */
  public void addPlatformWebResource( IPlatformWebResource resource ) {
    // see if we are already aware of the specified context
    if ( resource != null && !contextResourcesMap.containsKey( resource.getContext() ) ) {
      contextResourcesMap.put( resource.getContext(), new HashSet<>() );
    }
    contextResourcesMap.get( resource.getContext() ).add( resource );
  }

  public void removePlatformWebResource( IPlatformWebResource resource ) {
    if ( resource != null && contextResourcesMap.containsKey( resource.getContext() ) ) {
      contextResourcesMap.get( resource.getContext() ).remove( resource );
    }
  }

  void appendJsWebResources( StringBuilder sb, List<String> resources ) {
    resources.stream()
      .forEach( s -> {
        if ( s.startsWith( "/" ) ) {
          s = s.substring( 1 );
        }
        sb.append( "document.write(\"<script type='text/javascript' src='/"
          + s + "'></scr\"+\"ipt>\");\n" );
      } );
  }

  void appendCssWebResources( StringBuilder sb, List<String> resources ) {
    resources.stream()
      .forEach( s -> {
        if ( s.startsWith( "/" ) ) {
          s = s.substring( 1 );
        }
        sb.
          append( "document.write(\"<link rel='stylesheet' type='text/css' "
            + "href='/" + s + "'>\");\n" );
      } );
  }
}
