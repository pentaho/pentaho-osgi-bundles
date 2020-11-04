/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl.servlet;

import org.pentaho.requirejs.impl.RequireJsConfigManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class RequireJsConfigServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private String contextRoot;
  private RequireJsConfigManager manager;

  private String requireJsScript;

  public void setContextRoot( String contextRoot ) {
    // ensure that the given string is properly bounded with slashes
    contextRoot = ( !contextRoot.startsWith( "/" ) ) ? "/" + contextRoot : contextRoot;
    contextRoot = ( !contextRoot.endsWith( "/" ) ) ? contextRoot + "/" : contextRoot;

    this.contextRoot = contextRoot;
  }

  public void setManager( RequireJsConfigManager manager ) {
    this.manager = manager;
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
    resp.setContentType( "text/javascript" );

    // Prevent browser cache, as contents now might vary with the request's context
    resp.setHeader( "Cache-Control", "private, no-store, no-cache, must-revalidate" );
    resp.setHeader( "Pragma", "no-cache" );

    try ( PrintWriter printWriter = new PrintWriter( resp.getOutputStream() ) ) {
      RequestContext requestContext = new RequestContext( req );

      if ( requestContext.shouldOutputRequireJs() ) {
        printWriter.write( this.getRequireJsScript() );
      }

      printWriter.write( "\n(function(w) {" );

      String contextRoot = this.getContextRoot( requestContext );

      // ensure CONTEXT_PATH is defined
      printWriter.write( "\n  if (w.CONTEXT_PATH == null) {" );
      printWriter.write( "\n    w.CONTEXT_PATH = \"" + contextRoot + "\";" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      // store webcontext.js' requirejs module configurations if existing
      printWriter.write( "\n  var legacyConfig = null;" );
      printWriter.write( "\n  var legacyWaitSeconds = null;" );
      printWriter.write( "\n  var environment = null;" );
      printWriter.write( "\n  if (w.requireCfg != null) {" );
      printWriter.write( "\n    if (w.requireCfg.waitSeconds != null) {" );
      printWriter.write( "\n      legacyWaitSeconds = w.requireCfg.waitSeconds;" );
      printWriter.write( "\n    }" );
      printWriter.write( "\n    if (w.requireCfg.config != null) {" );
      printWriter.write( "\n      legacyConfig = w.requireCfg.config;" );
      printWriter.write( "\n      environment = legacyConfig[\"pentaho/environment\"];" );
      printWriter.write( "\n    }" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      // auxiliary function that allows packages' scripts to access its mapping information
      printWriter.write( "  function getVersionedModuleId(moduleIdsMappings, moduleId) {\n" );
      printWriter.write( "    if (moduleId.indexOf(\"!\") != -1) {\n" );
      printWriter.write( "      var parts = moduleId.split(\"!\", 2).slice(0);\n" );
      printWriter.write( "      return getVersionedModuleId(moduleIdsMappings, parts[0]) + \"!\" + getVersionedModuleId(moduleIdsMappings, parts[1]);\n" );
      printWriter.write( "    }\n" );
      printWriter.write( "    \n" );
      printWriter.write( "    var baseModuleId = moduleId;\n" );
      printWriter.write( "\n" );
      printWriter.write( "    if (!moduleIdsMappings.hasOwnProperty(moduleId)) {\n" );
      printWriter.write( "      var longestBaseModuleId = \"\";\n" );
      printWriter.write( "      for(var candidateBaseModuleId in moduleIdsMappings) {\n" );
      printWriter.write( "        if(moduleId.indexOf(candidateBaseModuleId) === 0 && candidateBaseModuleId.length > longestBaseModuleId.length) {\n" );
      printWriter.write( "          longestBaseModuleId = candidateBaseModuleId;\n" );
      printWriter.write( "        }\n" );
      printWriter.write( "      }\n" );
      printWriter.write( "\n" );
      printWriter.write( "      if(longestBaseModuleId.length === 0) {\n" );
      printWriter.write( "        return moduleId;\n" );
      printWriter.write( "      }\n" );
      printWriter.write( "\n" );
      printWriter.write( "      baseModuleId = longestBaseModuleId;\n" );
      printWriter.write( "    }\n" );
      printWriter.write( "\n" );
      printWriter.write( "    var versionedBaseModuleId = moduleIdsMappings[baseModuleId];\n" );
      printWriter.write( "    var moduleIdLeaf = moduleId.substring(baseModuleId.length);\n" );
      printWriter.write( "    if (moduleIdLeaf.length > 0 && moduleIdLeaf.indexOf(\"/\") !== 0) {\n" );
      printWriter.write( "      // false positive, we just caught a substring (probably some old mapping that included an hardcoded version)\n" );
      printWriter.write( "      return moduleId;\n" );
      printWriter.write( "    }\n" );
      printWriter.write( "\n" );
      printWriter.write( "    return versionedBaseModuleId + moduleIdLeaf;\n" );
      printWriter.write( "  }\n" );

      printWriter.write( "\n  var requireCfg = " + this.manager.getRequireJsConfig( contextRoot ) + "\n" );

      // Ensure embeddability: http://requirejs.org/docs/api.html#config-skipDataMain
      printWriter.write( "\n  requireCfg.skipDataMain = true;" );
      printWriter.write( "\n" );

      // set the legacy waitSeconds
      printWriter.write( "\n  if (legacyWaitSeconds != null) {" );
      printWriter.write( "\n    requireCfg.waitSeconds = legacyWaitSeconds;" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      // merge the requirejs module's configurations (first level only) to avoid overwriting them
      printWriter.write( "\n  if (legacyConfig != null) {" );
      printWriter.write( "\n    for (var key in legacyConfig) {" );
      printWriter.write( "\n      if (Object.prototype.hasOwnProperty.call(legacyConfig, key)) {" );
      printWriter.write( "\n        if (!requireCfg.config[key]) {" );
      printWriter.write( "\n          requireCfg.config[key] = {};" );
      printWriter.write( "\n        }" );
      printWriter.write( "\n" );
      printWriter.write( "\n        for (var moduleId in legacyConfig[key]) {" );
      printWriter.write( "\n          if (Object.prototype.hasOwnProperty.call(legacyConfig[key], moduleId)) {" );
      printWriter.write( "\n            requireCfg.config[key][moduleId] = legacyConfig[key][moduleId];" );
      printWriter.write( "\n          }" );
      printWriter.write( "\n        }" );
      printWriter.write( "\n      }" );
      printWriter.write( "\n    }" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      printWriter.write( "\n  require.config(requireCfg);" );

      // setup contextual mappings if the referer corresponds to a known package
      String cachedContextMapping = this.manager.getContextMapping( contextRoot, requestContext.getReferer() );
      if ( cachedContextMapping != null ) {
        printWriter.write( "\n  var contextMappingCfg = " + cachedContextMapping + ";\n" );
        printWriter.write( "\n  require.config(contextMappingCfg);" );
      }

      printWriter.write( "\n}(window));\n" );
    }
  }

  private String getContextRoot( RequestContext requestContext ) {
    return ( requestContext.shouldUseFullyQualifiedUrl() ? requestContext.getServerAddress() : "" ) + this.contextRoot;
  }

  private String getRequireJsScript() throws IOException {
    if ( this.requireJsScript == null ) {
      InputStream inputStream = null;
      InputStreamReader inputStreamReader = null;
      BufferedReader reader = null;
      try {
        inputStream = this.getClass().getClassLoader().getResourceAsStream( "js/require.js" );
        inputStreamReader = new InputStreamReader( inputStream );
        reader = new BufferedReader( inputStreamReader );
        String line;
        StringBuilder sb = new StringBuilder();
        while ( ( line = reader.readLine() ) != null ) {
          sb.append( line );
          sb.append( "\n" );
        }

        this.requireJsScript = sb.toString();
      } finally {
        if ( inputStreamReader != null ) {
          inputStreamReader.close();
        }
        if ( reader != null ) {
          reader.close();
        }
        if ( inputStream != null ) {
          inputStream.close();
        }
      }
    }

    return this.requireJsScript;
  }

  public class RequestContext {

    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private final boolean outputRequireJs;
    private final boolean useFullyQualifiedUrl;

    private final String serverAddress;

    private final String referer;

    RequestContext( HttpServletRequest req ) {
      // should the requirejs lib code be outputted? (defaults to true)
      this.outputRequireJs = this.getBooleanValue( req.getParameter( "requirejs" ), true );

      this.referer = req.getHeader( "referer" );

      // To be congruent with referer other popular http clients the port should be stripped from the 'Host' field
      // when the port is 80 or 443.
      this.serverAddress = ( HTTP_DEFAULT_PORT == req.getServerPort() || HTTPS_DEFAULT_PORT == req.getServerPort() )
        ? req.getScheme() + "://" + req.getServerName()
        : req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();

      // should the CONTEXT_PATH / baseUrl be a fully qualified URL?
      // (defaults to automatically determined using the request's referer)
      this.useFullyQualifiedUrl = this.getBooleanValue( req.getParameter( "useFullyQualifiedUrl" ), referer != null && !referer.startsWith( this.serverAddress ) );
    }

    boolean shouldOutputRequireJs() {
      return this.outputRequireJs;
    }

    boolean shouldUseFullyQualifiedUrl() {
      return this.useFullyQualifiedUrl;
    }

    String getServerAddress() {
      return this.serverAddress;
    }

    public String getReferer() {
      return referer;
    }

    private boolean getBooleanValue( String parameter, boolean defaultValue ) {
      if ( parameter == null ) {
        return defaultValue;
      }

      return Boolean.valueOf( parameter );
    }
  }
}
