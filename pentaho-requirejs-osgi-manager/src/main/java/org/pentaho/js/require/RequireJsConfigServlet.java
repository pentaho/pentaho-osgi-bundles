/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

/**
 * Created by bryan on 8/5/14.
 */
public class RequireJsConfigServlet extends HttpServlet {
  private final String requireJs;
  private RequireJsConfigManager manager;

  public RequireJsConfigServlet() throws IOException {
    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    try {
      inputStream = getClass().getClassLoader().getResourceAsStream( "/js/require.js" );
      inputStreamReader = new InputStreamReader( inputStream );
      reader = new BufferedReader( inputStreamReader );
      String line = null;
      StringBuilder sb = new StringBuilder();
      while ( ( line = reader.readLine() ) != null ) {
        sb.append( line );
        sb.append( "\n" );
      }
      requireJs = sb.toString();
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

  public RequireJsConfigManager getManager() {
    return manager;
  }

  public void setManager( RequireJsConfigManager manager ) {
    this.manager = manager;
  }

  @Override
  protected long getLastModified( HttpServletRequest req ) {
    return manager.getLastModified();
  }

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    resp.setContentType( "text/javascript" );
    PrintWriter printWriter = new PrintWriter( resp.getOutputStream() );
    try {
      printWriter.write( requireJs );
      printWriter.write( "\nrequireConfig = " );
      printWriter.write( manager.getRequireJsConfig() );
      printWriter.write( ";\n" );
      String config = req.getParameter( "config" );
      if ( config == null || Boolean.valueOf( config ) ) {
        printWriter.write( "require.config(requireConfig);" );
      }
    } finally {
      printWriter.close();
    }
  }
}
