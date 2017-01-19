/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/15/14.
 */
public class RequireJsConfigServletTest {
  private RequireJsConfigManager requireJsConfigManager;
  private RequireJsConfigServlet requireJsConfigServlet;

  @Before
  public void setup() throws IOException {
    requireJsConfigManager = mock( RequireJsConfigManager.class );
    requireJsConfigServlet = new RequireJsConfigServlet();
    requireJsConfigServlet.setManager( requireJsConfigManager );
  }

  @Test
  public void testGetManager() throws IOException {
    assertEquals( requireJsConfigManager, requireJsConfigServlet.getManager() );
  }

  @Test
  public void testGetLastModified() {
    when( requireJsConfigManager .getLastModified() ).thenReturn( 10L );
    assertEquals( 10L, requireJsConfigServlet.getLastModified( null ) );
  }

  @Test
  public void testDoGetWithConfig() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );
    assertTrue( outputStream.toString( "UTF-8" ).contains( testConfig ) );
    assertTrue( outputStream.toString( "UTF-8" ).contains( "require.config(requireCfg);" ) );
  }

  @Test
  public void testDoGetWithConfigTrue() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getParameter( "config" ) ).thenReturn( "true" );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );
    assertTrue( outputStream.toString( "UTF-8" ).contains( testConfig ) );
    assertTrue( outputStream.toString( "UTF-8" ).contains( "require.config(requireCfg);" ) );
  }

  @Test
  public void testDoGetWithoutConfig() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameter( "config" ) ).thenReturn( "false" );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );
    assertTrue( outputStream.toString( "UTF-8" ).contains( testConfig ) );
    assertFalse( outputStream.toString( "UTF-8" ).endsWith( "require.config(requireCfg);" ) );
  }


  @Test
  public void testDoGetWithoutRequireScript() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameter( "requirejs" ) ).thenReturn( "false" );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );

    assertFalse( outputStream.toString( "UTF-8" ).contains( "var requirejs, require, define;" ) );
  }


  @Test
  public void testDoGetWithRequireScript() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameter( "requirejs" ) ).thenReturn( "true" );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );

    assertTrue( outputStream.toString( "UTF-8" ).contains( "var requirejs, require, define;" ) );
  }


  @Test
  public void testDoGetWithRequireScriptByDefault() throws ServletException, IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    requireJsConfigServlet.doGet( request, response );

    assertTrue( outputStream.toString( "UTF-8" ).contains( "var requirejs, require, define;" ) );
  }


  @Test
  public void testSetContextRoot() throws ServletException, IOException {

    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameter( "config" ) ).thenReturn( "false" );
    HttpServletResponse response = mock( HttpServletResponse.class );
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(  );
    when( response.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override public void write( int b ) throws IOException {
        outputStream.write( b );
      }
    } );
    String testConfig = "TEST_CONFIG";
    when( requireJsConfigManager.getRequireJsConfig() ).thenReturn( testConfig );
    when( requireJsConfigManager.getContextRoot() ).thenReturn("/test/root/");

    requireJsConfigServlet.doGet( request, response );
    String output = outputStream.toString( "UTF-8" );
    assertTrue( output.contains( "requireCfg.baseUrl = '/test/root/" ) );

  }
}
