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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPlatformWebResource;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
    webContextServlet = new WebContextServlet();
    jsFile = new PlatformWebResource( "analyzer", "scripts/includeMe.js" );
    txtFile = new PlatformWebResource( "analyzer", "scripts/includeMe.txt" );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getRequestURI() ).thenReturn( "fake/uri/" + WebContextServlet.WEB_CONTEXT_JS );
    when( mockRequest.getParameter( WebContextServlet.CONTEXT ) ).thenReturn( "testContext" );
    when( mockRequest.getParameter( WebContextServlet.LOCALE ) ).thenReturn( "xp_TO" );

    this.httpRequest = mockRequest;

    HttpServletResponse mockResponse = mock( HttpServletResponse.class );


    this.mockResponseOutputStream = new java.io.ByteArrayOutputStream();
    when( mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public void write( int b ) throws IOException {
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

    writer.flush();
    String response = this.mockResponseOutputStream.toString();

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

    writer.flush();
    String response = this.mockResponseOutputStream.toString();

    resources.forEach( resource -> {
      String expected = getDocumentWriteExpected( resource );

      assertTrue( response.contains( expected ) );
    } );

  }

  @Test
  public void testDoGetRequireCfgCreated() throws ServletException, IOException {
    this.webContextServlet.doGet( this.httpRequest, this.httpResponse );
    String response = this.mockResponseOutputStream.toString();
    assertNotNull( response );
  }

  private String getDocumentWriteExpected( String resource ) {
    String location = "'\" + CONTEXT_PATH + \"" + resource + "'";

    if ( resource.endsWith( ".js" ) ) {
      return "document.write(\"<script type='text/javascript' src=" + location + "></scr\" + \"ipt>\");\n";
    } else {
      return "document.write(\"<link rel='stylesheet' type='text/css' href=" + location + ">\");\n";
    }
  }
}