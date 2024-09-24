/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPlatformWebResource;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import org.pentaho.di.core.Const;

@RunWith( MockitoJUnitRunner.class )
public class WebContextServletTest {
  private WebContextServlet webContextServlet;

  private IPlatformWebResource jsFile;
  private IPlatformWebResource txtFile;

  private HttpServletRequest httpRequest;
  private HttpServletResponse httpResponse;
  private ByteArrayOutputStream mockResponseOutputStream;

  @Before
  public void setUp() throws Exception {
    webContextServlet = spy( new WebContextServlet() );
    jsFile = new PlatformWebResource( "analyzer", "scripts/includeMe.js" );
    txtFile = new PlatformWebResource( "analyzer", "scripts/includeMe.txt" );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getRequestURI() ).thenReturn( "fake/uri/" + WebContextServlet.WEB_CONTEXT_JS );
    when( mockRequest.getParameter( WebContextServlet.CONTEXT ) ).thenReturn( "testContext" );
    when( mockRequest.getParameter( WebContextServlet.APPLICATION ) ).thenReturn( "testApplication" );
    when( mockRequest.getParameter( WebContextServlet.LOCALE ) ).thenReturn( "xp_TO" );

    this.httpRequest = mockRequest;

    HttpServletResponse mockResponse = mock( HttpServletResponse.class );


    this.mockResponseOutputStream = new java.io.ByteArrayOutputStream();
    when( mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        // noop
      }

      @Override
      public void write( int b ) {
        WebContextServletTest.this.mockResponseOutputStream.write( b );
      }
    } );

    this.httpResponse = mockResponse;
  }

  @Test
  public void testGetWebResources_NoMatches() throws Exception {
    List<String> webResources = webContextServlet.getWebResources( "analyzer", ".*\\.js" );

    assertNotNull( webResources );
    assertEquals( 0, webResources.size() );
  }

  @Test
  public void testGetWebResources_Match() throws Exception {
    webContextServlet.addPlatformWebResource( jsFile );
    webContextServlet.addPlatformWebResource( txtFile );

    List<String> webResources = webContextServlet.getWebResources( "analyzer", ".*\\.js" );

    assertNotNull( webResources );
    assertEquals( 1, webResources.size() );
    assertEquals( "scripts/includeMe.js", webResources.get( 0 ) );
  }

  @Test
  public void testWriteWebResourcesJSToDoc() throws Exception {
    List<String> resources = new ArrayList<>();
    resources.add( "scripts/includeMe.js" );
    resources.add( "scripts/includeMeToo.js" );

    PrintWriter writer = new PrintWriter( this.mockResponseOutputStream );
    this.webContextServlet.writeWebResources( writer, resources );

    String response = getServletResponse( writer );

    resources.forEach( resource -> {
      String expected = getDocumentWriteExpected( resource );

      assertTrue( response.contains( expected ) );

    } );

  }

  @Test
  public void testWriteWebResourcesCssToDoc() throws Exception {
    List<String> resources = new ArrayList<>();
    resources.add( "styles/awesome.css" );

    PrintWriter writer = new PrintWriter( this.mockResponseOutputStream );
    this.webContextServlet.writeWebResources( writer, resources );

    String response = getServletResponse( writer );

    resources.forEach( resource -> {
      String expected = getDocumentWriteExpected( resource );

      assertTrue( response.contains( expected ) );
    } );

  }

  @Test
  public void testWebContextDefinesContextPath() throws ServletException, IOException {
    final String response = doGetWebContextServlet();

    String contextPath = WebContextServlet.CONTEXT_PATH;
    assertTrue( response.contains( getWebContextVarDefinition( "CONTEXT_PATH", contextPath ) ) );
  }

  @Test
  public void testWebContextDefinesSessionLocale() throws ServletException, IOException {
    String sessionLocale = "fo_BA";
    when( this.httpRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );

    final String response = doGetWebContextServlet();

    assertTrue( response.contains( getWebContextVarDefinition( "SESSION_LOCALE", sessionLocale ) ) );
  }

  @Test
  public void testDoGetDefinesRequireCfg() throws ServletException, IOException {
    Integer waitTime = 1337;
    doReturn( waitTime ).when( this.webContextServlet ).getRequireWaitTime();

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
  public void testWebContextDefinesPentahoEnvironmentModuleConfig() throws ServletException, IOException {
    String mockRoot = "/root/";
    doReturn( mockRoot ).when( this.webContextServlet ).getServerRoot();

    String mockServerPackages = mockRoot + "osgi/";
    doReturn( mockServerPackages ).when( this.webContextServlet ).getServerPackages();

    String mockServices = mockRoot + "services/";
    doReturn( mockServices ).when( this.webContextServlet ).getServerServices();

    String serverRoot = escapeEnvironmentVariable( mockRoot );
    String serverPackages = escapeEnvironmentVariable( mockServerPackages );
    String serverServices = escapeEnvironmentVariable( mockServices );

    String sessionLocale = "fo_BA";
    when( this.httpRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );

    String application = "testApplication";
    when( this.httpRequest.getParameter( "application" ) ).thenReturn( application );

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

  // region Auxiliary Methods
  private String doGetWebContextServlet() throws ServletException, IOException {
    this.webContextServlet.doGet( this.httpRequest, this.httpResponse );
    return getServletResponse();
  }

  private String getServletResponse() throws IOException {
    return getServletResponse( null );
  }

  private String getServletResponse( PrintWriter writer ) throws IOException {
    if ( writer != null ) {
      writer.flush();
    }

    return this.mockResponseOutputStream.toString( "UTF-8" );
  }

  private String getWebContextVarDefinition( String variable, String value ) {
    String escapedValue = escapeEnvironmentVariable( value );

    return "\n/** @deprecated - use 'pentaho/environment' module's variable instead */" +
            "\nvar " + variable + " = " + escapedValue + ";";
  }

  private String getDocumentWriteExpected( String resource ) {
    String location = Const.isRunningOnWebspoonMode()? "'\" + CONTEXT_PATH + \"/" + resource + "'" : "'\" + CONTEXT_PATH + \"" + resource + "'";

    if ( resource.endsWith( ".js" ) ) {
      return "document.write(\"<script type='text/javascript' src=" + location + "></scr\" + \"ipt>\");\n";
    } else {
      return "document.write(\"<link rel='stylesheet' type='text/css' href=" + location + ">\");\n";
    }
  }

  private String escapeEnvironmentVariable( String value ) {
    return "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";
  }
  // endregion
}
