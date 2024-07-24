/*!
 * Copyright 2010 - 2021 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.platform.pdi;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.platform.api.engine.IPlatformWebResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WebContextServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  // this is the map that groups contexts to a set of urls to add to the context
  private Map<String, Set<IPlatformWebResource>> contextResourcesMap = new HashMap<>();

  static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  static final String CONTEXT_PATH = Const.isRunningOnWebspoonMode() ? "/spoon/osgi" : "/";
  private static final String REQUIREJS_INIT_LOCATION = Const.isRunningOnWebspoonMode() ? "requirejs-manager/js/require-init.js?useFullyQualifiedUrl=false" : "requirejs-manager/js/require-init.js";

  private static final String DEFAULT_SERVICES_ROOT = "cxf/";
  private static final Integer DEFAULT_WAIT_TIME = 30;

  static final String CONTEXT = "context";
  static final String LOCALE = "locale";
  static final String APPLICATION = "application";

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

  @Override
  protected void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
      throws ServletException, IOException {

    String requestStr = httpRequest.getRequestURI();
    if ( requestStr != null && requestStr.contains( WEB_CONTEXT_JS ) ) {

      httpResponse.setContentType( "text/javascript" ); //$NON-NLS-1$

      try ( PrintWriter printWriter = new PrintWriter( httpResponse.getOutputStream() ) ) {
        writeWebContextVar( printWriter, "dojoConfig", "[]", false, false );

        writeWebContextVar( printWriter, "CONTEXT_PATH", CONTEXT_PATH );

        writeWebContextVar( printWriter, "IS_RUNNING_ON_WEBSPOON_MODE", String.valueOf( Const.isRunningOnWebspoonMode() ), false, false );

        String locale = getLocale( httpRequest );
        writeWebContextVar( printWriter, "SESSION_LOCALE", locale );
        writeLocaleModule( printWriter, locale );

        writeWebContextVar( printWriter, "requireCfg", getRequireCfg(), false, false );
        writeEnvironmentModuleConfig( printWriter, httpRequest );

        writeDocumentWriteResource( printWriter, REQUIREJS_INIT_LOCATION );

        writeJsWebResources( printWriter, httpRequest );
        writeCssWebResources( printWriter, httpRequest );
      }
    } else {
      httpResponse.sendError( 404 );
    }

  }

  @Override
  public void destroy() {

  }

  // region Write Methods
  private void writeWebContextVar( PrintWriter writer, String variable, String value ) {
    writeWebContextVar( writer, variable, value, true, true );
  }

  private void writeWebContextVar( PrintWriter writer, String variable, String value,
                                   boolean deprecated, boolean escapeValue ) {
    if ( escapeValue ) {
      value = escapeEnvironmentVar( value );
    }

    if ( deprecated ) {
      writer.write( "\n/** @deprecated - use 'pentaho/environment' module's variable instead */" );
    }

    writer.write( "\nvar " + variable + " = " + value + ";\n" );
  }

  private void writeLocaleModule( PrintWriter writer, String value ) {
    value = escapeEnvironmentVar( value );

    writer.write( "// If RequireJs is available, supply a module" );
    writer.write( "\nif (typeof(pen) !== 'undefined' && pen.define) {" );
    writer.write( "\n  pen.define('Locale', { locale: " + value + " });" );
    writer.write( "\n}\n" );
  }

  private void writeEnvironmentModuleConfig( PrintWriter writer, HttpServletRequest request ) {
    String application = escapeEnvironmentVar( getApplication( request ) );
    String locale = escapeEnvironmentVar( getLocale( request ) );
    String serverRoot = escapeEnvironmentVar( getServerRoot() );
    String serverPackages = escapeEnvironmentVar( getServerPackages() );

    String serverServices = escapeEnvironmentVar( getServerServices() );

    writer.write( "\nrequireCfg.config[\"pentaho/environment\"] = {" );
    writer.write( "\n  application: " + application + "," );
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

  private void writeJsWebResources( PrintWriter writer, HttpServletRequest request ) {
    String contextName = getContextName( request );
    List<String> resources = getWebResources( contextName, ".*\\.js" );

    writeWebResources( writer, resources );
  }

  private void writeCssWebResources( PrintWriter writer, HttpServletRequest request ) {
    String contextName = getContextName( request );
    List<String> resources = getWebResources( contextName, ".*\\.css" );

    writeWebResources( writer, resources );
  }

  void writeWebResources( PrintWriter writer, List<String> resources ) {
    resources.stream().forEach( location -> {
      if ( location.startsWith( "/" ) ) {
        location = location.substring( 1 );
      }

      writeDocumentWriteResource( writer, location );
    } );
  }

  private void writeDocumentWriteResource( PrintWriter writer, String location ) {
    boolean isJavascript = Const.isRunningOnWebspoonMode() ? location.contains( ".js" ) : location.endsWith( ".js" );

    writer.write( "document.write(\"" );

    if ( isJavascript ) {
      writer.write( "<script type='text/javascript' src=" );
    } else {
      writer.write( "<link rel='stylesheet' type='text/css' href=" );
    }

    if ( Const.isRunningOnWebspoonMode() ) {
      writer.write( "'\" + CONTEXT_PATH + \"/" + location + "'>" );
    } else {
      writer.write( "'\" + CONTEXT_PATH + \"" + location + "'>" );
    }

    writer.append(  isJavascript ? ( "</scr\" + \"ipt>" ) : "" );
    writer.write( "\");\n" );
  }
  // endregion

  private String escapeEnvironmentVar( String variable ) {
    if ( variable == null ) {
      return null;
    }

    return "\"" + StringEscapeUtils.escapeJavaScript( variable ) + "\"";
  }

  List<String> getWebResources( String context, String filePattern ) {
    Set<IPlatformWebResource> resources = this.contextResourcesMap.get( context );

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

  /**
   * Gets the identifier of the application from the http request.
   *
   * @param request - The http request.
   *
   * @return the application identifier.
   */
  private String getApplication( HttpServletRequest request ) {
    return request.getParameter( APPLICATION );
  }

  /**
   * Gets Hitachi Vantara context name from the http request.
   *
   * @param request - The http request.
   *
   * @return the Pentaho context name.
   */
  private String getContextName( HttpServletRequest request ) {
    String context = request.getParameter( CONTEXT );

    return StringUtils.isNotEmpty( context ) ? context : null;
  }

  String getServerRoot() {
    return CONTEXT_PATH;
  }

  String getServerPackages() {
    return CONTEXT_PATH;
  }

  String getServerServices() {
    String servicesRoot = getServicesRoot();
    boolean isRootValid = StringUtils.isNotEmpty( servicesRoot );

    if ( isRootValid && servicesRoot.startsWith( "/" ) ) {
      servicesRoot = servicesRoot.substring( 1 );
    }

    if ( isRootValid && !servicesRoot.endsWith( "/" ) ) {
      servicesRoot = servicesRoot + "/";
    }

    return CONTEXT_PATH + servicesRoot;
  }

  /**
   * Gets the base structure of the `requireCfg` javascript object.
   *
   * @return
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
            .append( "\n  config: { \"pentaho/modules\": {} }," )
            .append( "\n  packages: []" )
            .append( "\n}" );

    return requireCfg.toString();
  }

  /**
   * Add any resource to the web context.
   *
   * @param resource - the platform web resource.
   */
  public void addPlatformWebResource( IPlatformWebResource resource ) {
    if ( resource == null ) {
      return;
    }

    String resourceContext = resource.getContext();
    // see if we are already aware of the specified context
    if ( !this.contextResourcesMap.containsKey( resourceContext ) ) {
      this.contextResourcesMap.put( resourceContext, new HashSet<>() );
    }

    this.contextResourcesMap.get( resourceContext ).add( resource );
  }

  /**
   * Remove any resource from the web context.
   *
   * @param resource - the platform web resource.
   */
  public void removePlatformWebResource( IPlatformWebResource resource ) {
    if ( resource == null ) {
      return;
    }

    String resourceContext = resource.getContext();
    if ( this.contextResourcesMap.containsKey( resourceContext ) ) {
      this.contextResourcesMap.get( resourceContext ).remove( resource );
    }
  }
}
