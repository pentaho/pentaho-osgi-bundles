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
package org.pentaho.webcontext.core.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class PentahoWebContextServletImpl extends HttpServlet {
  private static final String WEB_CONTEXT_JS = "webcontext.js";

  private static final String SERVER_ROOT = "/";
  private static final String REQUIREJS_INIT_LOCATION = "requirejs-manager/js/require-init.js";

  private static final String DEFAULT_SERVICES_ROOT = "cxf/";
  private static final Integer DEFAULT_WAIT_TIME = 30;

  private static final String LOCALE_REQUEST_PARAM = "locale";

  // region Blueprint Injection
  private Integer requireWaitTime;
  private String servicesRoot;

  public void setRequireWaitTime( Integer value ) {
    this.requireWaitTime = value;
  }

  public Integer getRequireWaitTime() {
    Integer waitTime = this.requireWaitTime;

    if ( waitTime == null ) {
      waitTime = DEFAULT_WAIT_TIME;
    }

    return waitTime;
  }

  public void setServicesRoot( String value ) {
    this.servicesRoot = value;
  }

  public String getServicesRoot() {
    String servicesRoot = this.servicesRoot;
    if ( StringUtils.isEmpty( servicesRoot ) ) {
      servicesRoot = DEFAULT_SERVICES_ROOT;
    }

    return servicesRoot;
  }
  // endregion

  @Override
  protected void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse ) throws IOException {

    String requestStr = httpRequest.getRequestURI();
    if ( requestStr != null && requestStr.contains( WEB_CONTEXT_JS ) ) {
      httpResponse.setContentType( "text/javascript" ); //$NON-NLS-1$

      try ( PrintWriter printWriter = new PrintWriter( httpResponse.getOutputStream() ) ) {
        writeRequireCfg( printWriter );

        writeEnvironmentModuleConfig( printWriter, httpRequest );

        writeRequireJsInitScriptTag( printWriter );
      }

    } else {
      httpResponse.sendError( 404 );

    }

  }

  @Override
  public void destroy() {

  }

  // region Write Methods
  private void writeRequireCfg( PrintWriter writer ) {
    writer.write( "\nvar requireCfg = " + getRequireCfg() + ";\n" );
  }

  private void writeEnvironmentModuleConfig( PrintWriter writer, HttpServletRequest request ) {
    String locale = escapeEnvironmentVar( getLocale( request ) );
    String serverRoot = escapeEnvironmentVar( getServerRoot() );
    String serverPackages = escapeEnvironmentVar( getServerPackages() );

    String serverServices = escapeEnvironmentVar( getServerServices() );

    writer.write( "\nrequireCfg.config[\"pentaho/environment\"] = {" );
    writer.write( "\n  theme: null," );
    writer.write( "\n  locale: " + locale + "," );
    writer.write( "\n  user: {" );
    writer.write( "\n    id: null," );
    writer.write( "\n    home: null" );
    writer.write( "\n  }," );
    writer.write( "\n  server: {" );
    writer.write( "\n    root: " + serverRoot + "," );
    writer.write( "\n    packages: " + serverPackages + "," );
    writer.write( "\n    services: " + serverServices );
    writer.write( "\n  }," );
    writer.write( "\n  reservedChars: null" );
    writer.write( "\n};\n" );
  }

  private void writeRequireJsInitScriptTag( PrintWriter writer ) {
    String location = SERVER_ROOT + REQUIREJS_INIT_LOCATION;

    writer.write( "\ndocument.write(\"<script type='text/javascript' src=" + location + "'></scr\" + \"ipt>\");\n" );
  }
  // endregion

  private String escapeEnvironmentVar( String variable ) {
    if ( variable == null ) {
      return null;
    }

    return "\"" + StringEscapeUtils.escapeJavaScript( variable ) + "\"";
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
    String locale = request.getParameter( LOCALE_REQUEST_PARAM );
    if ( StringUtils.isEmpty( locale ) ) {
      locale = Locale.getDefault().toString();
    }

    return locale;
  }

  // region
  private String getServerRoot() {
    return SERVER_ROOT;
  }

  private String getServerPackages() {
    return SERVER_ROOT;
  }

  private String getServerServices() {
    String servicesRoot = getServicesRoot();
    boolean isRootValid = StringUtils.isNotEmpty( servicesRoot );

    if ( isRootValid && servicesRoot.startsWith( "/" ) ) {
      servicesRoot = servicesRoot.substring( 1 );
    }

    if ( isRootValid && !servicesRoot.endsWith( "/" ) ) {
      servicesRoot = servicesRoot + "/";
    }

    return SERVER_ROOT + servicesRoot;
  }

  /**
   * Gets the base structure of the `requireCfg` javascript object.
   */
  private String getRequireCfg() {
    // setup a RequireJS config object for plugins to extend
    StringBuilder requireCfg = new StringBuilder();

    requireCfg
      .append( "{" )
      .append( "\n  waitSeconds: " ).append( getRequireWaitTime() ).append( "," )
      .append( "\n  paths: {}," )
      .append( "\n  shim: {}," )
      .append( "\n  map: { \"*\": {} }," )
      .append( "\n  bundles: {}," )
      .append( "\n  config: { \"pentaho/service\": {} }," )
      .append( "\n  packages: []" )
      .append( "\n}" );

    return requireCfg.toString();
  }

}
