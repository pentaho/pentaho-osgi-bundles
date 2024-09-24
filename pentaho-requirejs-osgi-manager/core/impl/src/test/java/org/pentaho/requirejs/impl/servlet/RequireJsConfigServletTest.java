/*!
 * Copyright 2020 Hitachi Vantara.  All rights reserved.
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