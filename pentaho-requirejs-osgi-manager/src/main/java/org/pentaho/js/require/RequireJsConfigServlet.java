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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.js.require;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class RequireJsConfigServlet extends HttpServlet {
  private String contextRoot;
  private RequireJsConfigManager manager;

  private String requireJs;

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
  protected long getLastModified( HttpServletRequest req ) {
    return this.manager.getLastModified();
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    resp.setContentType( "text/javascript" );
    resp.setHeader( "Cache-Control", "must-revalidate" );

    try ( PrintWriter printWriter = new PrintWriter( resp.getOutputStream() ) ) {
      RequestContext requestContext = new RequestContext( req );

      if ( requestContext.shouldOutputRequireJs() ) {
        printWriter.write( this.getRequireJsScript() );
      }

      printWriter.write( "\n(function(w) {" );

      // ensure CONTEXT_PATH is defined
      printWriter.write( "\n  if (w.CONTEXT_PATH == null) {" );
      printWriter.write( "\n    w.CONTEXT_PATH = \"" + this.getContextRoot( requestContext ) + "\";" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      // store webcontext.js' requirejs module configurations if existing
      printWriter.write( "\n  var legacyConfig = null;" );
      printWriter.write( "\n  if (w.requireCfg != null && w.requireCfg.config != null) {" );
      printWriter.write( "\n    legacyConfig = w.requireCfg.config;" );
      printWriter.write( "\n  }" );
      printWriter.write( "\n" );

      printWriter.write( "\n  var requireCfg = " + this.manager.getRequireJsConfig( this.getContextRoot( requestContext ) ) + "\n" );

      // Ensure embeddability: http://requirejs.org/docs/api.html#config-skipDataMain
      printWriter.write( "\n  requireCfg.skipDataMain = true;" );
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

      printWriter.write( "\n}(window));\n" );
    }
  }

  private String getContextRoot( RequestContext requestContext ) {
    return ( requestContext.shouldUseFullyQualifiedUrl() ? requestContext.getServerAddress() : "" ) + this.contextRoot;
  }

  private String getRequireJsScript() throws IOException {
    if ( this.requireJs == null ) {
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

        this.requireJs = sb.toString();
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

    return this.requireJs;
  }

  private class RequestContext {
    private final boolean outputRequireJs;
    private final boolean useFullyQualifiedUrl;

    private final String serverAddress;

    RequestContext( HttpServletRequest req ) {
      // should the requirejs lib code be outputted? (defaults to true)
      this.outputRequireJs = this.getBooleanValue( req.getParameter( "requirejs" ), true );

      final String referer = req.getHeader( "referer" );
      this.serverAddress = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();

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

    private boolean getBooleanValue( String parameter, boolean defaultValue ) {
      if ( parameter == null ) {
        return defaultValue;
      }

      return Boolean.valueOf( parameter );
    }
  }
}
