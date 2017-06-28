/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPlatformWebResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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

public class WebContextServlet extends HttpServlet {
  // this is the map that groups contexts to a set of urls to add to the context
  private Map<String, Set<IPlatformWebResource>> contextResourcesMap = new HashMap<>();
  protected static ICacheManager cache = PentahoSystem.getCacheManager( null );

  static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  static final String CONTEXT = "context";
  static final String LOCALE = "locale";

  @Override
  protected void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
      throws ServletException, IOException {

    String requestStr = httpRequest.getRequestURI();
    if ( requestStr != null && requestStr.contains( WEB_CONTEXT_JS ) ) {
      httpResponse.setContentType( "text/javascript" ); //$NON-NLS-1$

      String contextPath = "/";
      String contextName = getContextName( httpRequest );
      String effectiveLocale = getLocale( httpRequest );


      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder
              .append( "\n/** @deprecated - use 'pentaho/context' module's variable instead */" )
              .append( "\nvar CONTEXT_PATH = " ).append( escapeEnvironmentVar( contextPath ) ).append( ";\n" )
              .append( "\nvar dojoConfig = [];\n" )

              .append( "\nvar requireCfg = " ).append( getRequireCfg() )
              .append( "\nrequireCfg.config[\"pentaho/context\"] = {")
              .append( "\n  theme: null," )
              .append( "\n  locale: " ).append( escapeEnvironmentVar( effectiveLocale ) ).append( "," )
              .append( "\n  user: {" )
              .append( "\n    id: null," )
              .append( "\n    home: null" )
              .append( "\n  }," )
              .append( "\n  server: {" )
              .append( "\n    url: " ).append( escapeEnvironmentVar( contextPath ) )
              .append( "\n  }," )
              .append( "\n  reservedChars: null" )
              .append( "\n};\n" );

      appendLocale( stringBuilder, effectiveLocale );

      appendJsWebResources( stringBuilder, getWebResources( contextName, ".*\\.js" ) );
      appendCssWebResources( stringBuilder, getWebResources( contextName, ".*\\.css" ) );

      String requireJsLocation = "requirejs-manager/js/require-init.js";
      stringBuilder.append(
          "\ndocument.write(\"<script type='text/javascript' src='" + contextPath
              + requireJsLocation + "'></scr\"+\"ipt>\");\n" );

      httpResponse.getWriter().write( stringBuilder.toString() );
    } else {
      httpResponse.sendError( 404 );
    }

  }

  /**
   * Gets the base structure of the `requireCfg` javascript object.
   *
   * @return
   */
  private String getRequireCfg() {
    Integer waitTime = null;

    if ( cache != null ) {
      waitTime = (Integer) cache.getFromGlobalCache( PentahoSystem.WAIT_SECONDS );
    }

    if ( waitTime == null ) {
      try {
        waitTime = Integer.valueOf( PentahoSystem.getSystemSetting( PentahoSystem.WAIT_SECONDS, "30" ) );
      } catch ( NumberFormatException e ) {
        waitTime = 30;
      }
      if ( cache != null ) {
        cache.putInGlobalCache( PentahoSystem.WAIT_SECONDS, waitTime );
      }
    }

    return "{ waitSeconds: " + waitTime + ", paths: {}, shim: {}, " +
            "map: { \"*\": {} }, bundles: {}, config: { \"pentaho/service\": {} }, packages: [] };";
  }

  private String escapeEnvironmentVar( String variable ) {
    if ( variable == null ) {
      return null;
    }

    return "\"" + variable + "\"";
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

  /**
   * Gets the session locale from the http request. If no locale is defined,
   * as fallback, the default locale will be used instead.
   *
   * @param request - The http request.
   *
   * @return the session locale.
   */
  private String getLocale( HttpServletRequest request ) {
    // set the locale from PDI
    Locale defaultLocale = LanguageChoice.getInstance().getDefaultLocale();
    if ( defaultLocale == null ) {
      defaultLocale = Locale.getDefault();
    }

    String locale = request.getParameter( LOCALE );
    if ( StringUtils.isEmpty( locale ) ) {
      locale = defaultLocale.toString();
    }

    return locale;
  }

  private void appendLocale( StringBuilder sb, String locale ) {
    String localeJsString = escapeEnvironmentVar( locale );

    sb.append( "\n/** @deprecated - use 'pentaho/context' module's variable instead */" )
      .append( "\nvar SESSION_LOCALE = " + localeJsString + ";" ) // Global variable
      // If RequireJs is available, supply a module
      .append( "\nif (typeof(pen) != 'undefined' && pen.define) {" )
      .append( "\n  pen.define('Locale', { locale: " ).append( localeJsString ).append( " })" )
      .append( "\n};\n" );
  }

  /**
   * Gets Pentaho context name from the http request.
   *
   * @param request - The http request.
   *
   * @return the Pentaho context name.
   */
  private String getContextName( HttpServletRequest request ) {
    String context = request.getParameter( CONTEXT );

    return StringUtils.isNotEmpty( context ) ? context : null;
  }

  @Override
  public void destroy() {

  }

  /**
   * Add any resource to the web context
   *
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
