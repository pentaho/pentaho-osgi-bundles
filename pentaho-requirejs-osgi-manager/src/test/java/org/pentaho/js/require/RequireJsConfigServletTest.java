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

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequireJsConfigServletTest {
  private String contextRoot;
  private String serverAddress;
  private String testConfig;

  private RequireJsConfigManager mockRequireJsConfigManager;

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private ByteArrayOutputStream mockResponseOutputStream;

  private RequireJsConfigServlet requireJsConfigServlet;

  @Before
  public void setup() throws IOException {
    this.contextRoot = "/the/context/root/";

    this.testConfig = "{ \"mock\": \"This is just a mock require configuration!\" };";

    String scheme = "https";
    String serverName = "di.pentaho.local";
    int port = 9055;

    this.serverAddress = scheme + "://" + serverName + ":" + port;

    this.mockRequireJsConfigManager = mock( RequireJsConfigManager.class );

    when( this.mockRequireJsConfigManager.getRequireJsConfig( anyString() ) ).thenReturn( this.testConfig );

    this.requireJsConfigServlet = new RequireJsConfigServlet();
    this.requireJsConfigServlet.setManager( this.mockRequireJsConfigManager );
    this.requireJsConfigServlet.setContextRoot( this.contextRoot );

    this.mockRequest = mock( HttpServletRequest.class );

    when( this.mockRequest.getScheme() ).thenReturn( scheme );
    when( this.mockRequest.getServerName() ).thenReturn( serverName );
    when( this.mockRequest.getServerPort() ).thenReturn( port );

    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/some/app" );

    this.mockResponse = mock( HttpServletResponse.class );
    this.mockResponseOutputStream = new ByteArrayOutputStream();
    when( this.mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public void write( int b ) throws IOException {
        RequireJsConfigServletTest.this.mockResponseOutputStream.write( b );
      }
    } );
  }

  @Test
  public void testGetLastModified() {
    // last modification date should be the same than the last modification of the require configurations manager
    when( this.mockRequireJsConfigManager.getLastModified() ).thenReturn( 10L );

    final long lastModified = this.requireJsConfigServlet.getLastModified( this.mockRequest );

    assertEquals( 10L, lastModified );
    //noinspection ResultOfMethodCallIgnored
    verify( this.mockRequireJsConfigManager, times( 1 ) ).getLastModified();
  }

  @Test
  public void testDoGetWithDefaults() throws ServletException, IOException {
    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseIncludesRequireJsScript( response ) );
    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( this.contextRoot );
    assertTrue( this.responseDefinesRequireCfgVariable( response, this.testConfig ) );
    assertTrue( this.responseCallRequireConfig( response ) );
  }

  @Test
  public void testDoGetWithRequireJsTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "requirejs" ) ).thenReturn( "true" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseIncludesRequireJsScript( response ) );
  }

  @Test
  public void testDoGetWithRequireJsFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "requirejs" ) ).thenReturn( "false" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertFalse( this.responseIncludesRequireJsScript( response ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "useFullyQualifiedUrl" ) ).thenReturn( "true" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    String fullContextRoot = this.serverAddress + this.contextRoot;

    assertTrue( this.responseSetsContextPathGlobal( response, fullContextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( fullContextRoot );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "useFullyQualifiedUrl" ) ).thenReturn( "false" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( this.contextRoot );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultOutsideReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( "http://dashboard.somewhere.com/other/app" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    String fullContextRoot = this.serverAddress + this.contextRoot;

    assertTrue( this.responseSetsContextPathGlobal( response, fullContextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( fullContextRoot );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultLocalReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/other/app" );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( this.contextRoot );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultNullReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( null );

    this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    this.requireJsConfigManagerIsCalledWithTheProperContextRoot( this.contextRoot );
  }

  @Test
  public void testContextRootFixes() throws ServletException, IOException {
    String[] contextPermutations = new String[]{"partial/root", "/partial/root", "partial/root/"};

    for ( String contextPermutation : contextPermutations ) {
      reset( this.mockRequireJsConfigManager );

      this.requireJsConfigServlet.setContextRoot( contextPermutation );

      this.requireJsConfigServlet.doGet( this.mockRequest, this.mockResponse );

      final String response = this.mockResponseOutputStream.toString( "UTF-8" );

      assertTrue( this.responseSetsContextPathGlobal( response, "/partial/root/" ) );
      this.requireJsConfigManagerIsCalledWithTheProperContextRoot( "/partial/root/" );
    }
  }

  private boolean responseIncludesRequireJsScript( String response ) {
    return response.contains( "var requirejs, require, define;" );
  }

  private boolean responseSetsContextPathGlobal( String response, String contextRoot ) {
    return response.contains( "w.CONTEXT_PATH = \"" + contextRoot + "\";" );
  }

  private void requireJsConfigManagerIsCalledWithTheProperContextRoot( String contextRoot ) {
    verify( this.mockRequireJsConfigManager, times( 1 ) ).getRequireJsConfig( contextRoot );
  }

  private boolean responseDefinesRequireCfgVariable( String response, String requireConfiguration ) {
    return response.contains( "var requireCfg = " + requireConfiguration );
  }

  private boolean responseCallRequireConfig( String response ) {
    return response.contains( "require.config(requireCfg);" );
  }
}
