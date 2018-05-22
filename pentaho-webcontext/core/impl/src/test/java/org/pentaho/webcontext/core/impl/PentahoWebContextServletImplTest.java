/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
import org.junit.Before;
import org.junit.Test;
import org.pentaho.webcontext.core.impl.PentahoWebContextServletImpl;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.pentaho.webcontext.core.impl.PentahoWebContextServletImpl.LOCALE_REQUEST_PARAM;
import static org.pentaho.webcontext.core.impl.PentahoWebContextServletImpl.APPLICATION_REQUEST_PARAM;
import static org.pentaho.webcontext.core.impl.PentahoWebContextServletImpl.WEB_CONTEXT_JS;

public class PentahoWebContextServletImplTest {
  private PentahoWebContextServletImpl webContextServlet;

  private HttpServletRequest httpRequest;
  private HttpServletResponse httpResponse;
  private ByteArrayOutputStream mockResponseOutputStream;

  @Before
  public void setUp() throws Exception {
    webContextServlet = spy( new PentahoWebContextServletImpl() );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getRequestURI() ).thenReturn( "fake/uri/" + WEB_CONTEXT_JS );
    when( mockRequest.getParameter( LOCALE_REQUEST_PARAM ) ).thenReturn( "xp_TO" );

    this.httpRequest = mockRequest;

    HttpServletResponse mockResponse = mock( HttpServletResponse.class );


    this.mockResponseOutputStream = new java.io.ByteArrayOutputStream();
    when( mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public void write( int b ) throws IOException {
        PentahoWebContextServletImplTest.this.mockResponseOutputStream.write( b );
      }
    } );

    this.httpResponse = mockResponse;
  }

  @Test
  public void testDoGetDefinesRequireCfg() throws IOException {
    Integer waitTime = 1337;
    when( this.webContextServlet.getRequireWaitTime() ).thenReturn( waitTime );

    String response = doGetWebContextServlet();

    String expected = "var requireCfg = {" +
      "\n  waitSeconds: " + waitTime + "," +
      "\n  paths: {}," +
      "\n  shim: {}," +
      "\n  map: { \"*\": {} }," +
      "\n  bundles: {}," +
      "\n  config: { \"pentaho/modules\": {} }," +
      "\n  packages: []" +
      "\n}";

    assertTrue( response.contains( expected ) );
  }

  @Test
  public void testWebContextDefinesPentahoEnvironmentModuleConfig() throws IOException {
    String mockRoot = "/root/";
    when( this.webContextServlet.getServerRoot() ).thenReturn( mockRoot );

    String mockServerPackages = mockRoot + "osgi/";
    when( this.webContextServlet.getServerPackages() ).thenReturn( mockServerPackages );

    String mockServices = mockRoot + "services/";
    when( this.webContextServlet.getServerServices() ).thenReturn( mockServices );

    String serverRoot = escapeEnvironmentVariable( mockRoot );
    String serverPackages = escapeEnvironmentVariable( mockServerPackages );
    String serverServices = escapeEnvironmentVariable( mockServices );

    String sessionLocale = "fo_BA";
    when( this.httpRequest.getParameter( LOCALE_REQUEST_PARAM ) ).thenReturn( sessionLocale );

    String application = "fo_BA";
    when( this.httpRequest.getParameter( APPLICATION_REQUEST_PARAM ) ).thenReturn( application );

    final String response = doGetWebContextServlet();
    String environmentModuleConfig = "\nrequireCfg.config[\"pentaho/environment\"] = {" +
      "\n  application: \"" + application + "\"," +
      "\n  theme: null," +
      "\n  locale: \"" + sessionLocale + "\"," +
      "\n  user: {" +
      "\n    id: null," +
      "\n    home: null" +
      "\n  }," +
      "\n  server: {" +
      "\n    root: " + serverRoot + "," +
      "\n    packages: " + serverPackages + "," +
      "\n    services: " + serverServices +
      "\n  }," +
      "\n  reservedChars: null" +
      "\n}";

    assertTrue( response.contains( environmentModuleConfig ) );
  }

  @Test
  public void testRequireJsInitScriptTag() throws IOException {
    String location = "/path/to/requireJsInit.js";

    when( this.webContextServlet.getRequirejsInitLocation() ).thenReturn( location );

    String scriptTag = "document.write(\"<script type='text/javascript' src='" + location + "'></scr\" + \"ipt>\");";

    final String response = doGetWebContextServlet();
    assertTrue( response.contains( scriptTag ) );
  }

  // region Auxiliary Methods
  private String doGetWebContextServlet() throws IOException {
    this.webContextServlet.doGet( this.httpRequest, this.httpResponse );
    return getServletResponse();
  }

  private String getServletResponse() throws IOException {
    return this.mockResponseOutputStream.toString( "UTF-8" );
  }

  private String escapeEnvironmentVariable( String value ) {
    return "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";
  }
  // endregion
}
