/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.requirejs.impl.servlet;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequireJsConfigServletTest {

  @Test
  public void validateServerAddressConstructWithPort80Test() {

    HttpServletRequest httpServletRequestMock = mock( HttpServletRequest.class );

    String requestSchema = "http";
    String requestServerName = "server-name";
    Integer requestServerPort = 80;
    when( httpServletRequestMock.getScheme() ).thenReturn( requestSchema );
    when( httpServletRequestMock.getServerName() ).thenReturn( requestServerName );
    when( httpServletRequestMock.getServerPort() ).thenReturn( requestServerPort );

    RequireJsConfigServletForTest requireJsConfigServlet = new RequireJsConfigServletForTest();

    RequireJsConfigServlet.RequestContext requestContext =
      requireJsConfigServlet.createRequestContext( httpServletRequestMock );

    String serverAddress = requestSchema + "://" + requestServerName;
    assertEquals( serverAddress, requestContext.getServerAddress() );
  }

  @Test
  public void validateServerAddressConstructWithPort443Test() {

    HttpServletRequest httpServletRequestMock = mock( HttpServletRequest.class );

    String requestSchema = "https";
    String requestServerName = "server-name";
    Integer requestServerPort = 443;
    when( httpServletRequestMock.getScheme() ).thenReturn( requestSchema );
    when( httpServletRequestMock.getServerName() ).thenReturn( requestServerName );
    when( httpServletRequestMock.getServerPort() ).thenReturn( requestServerPort );

    RequireJsConfigServletForTest requireJsConfigServlet = new RequireJsConfigServletForTest();

    RequireJsConfigServlet.RequestContext requestContext =
      requireJsConfigServlet.createRequestContext( httpServletRequestMock );

    String serverAddress = requestSchema + "://" + requestServerName;
    assertEquals( serverAddress, requestContext.getServerAddress() );
  }


  @Test
  public void validateServerAddressConstructWithoutDefaultPortTest() {

    HttpServletRequest httpServletRequestMock = mock( HttpServletRequest.class );

    String requestSchema = "http";
    String requestServerName = "server-name";
    Integer requestServerPort = 8080;
    when( httpServletRequestMock.getScheme() ).thenReturn( requestSchema );
    when( httpServletRequestMock.getServerName() ).thenReturn( requestServerName );
    when( httpServletRequestMock.getServerPort() ).thenReturn( requestServerPort );

    RequireJsConfigServletForTest requireJsConfigServlet = new RequireJsConfigServletForTest();

    RequireJsConfigServlet.RequestContext requestContext =
      requireJsConfigServlet.createRequestContext( httpServletRequestMock );

    String serverAddress = requestSchema + "://" + requestServerName + ":" + requestServerPort;
    assertEquals( serverAddress, requestContext.getServerAddress() );
  }




  public class RequireJsConfigServletForTest extends RequireJsConfigServlet {

    public RequestContext createRequestContext( HttpServletRequest request ) {
      return new RequestContext( request );
    }
  }
}