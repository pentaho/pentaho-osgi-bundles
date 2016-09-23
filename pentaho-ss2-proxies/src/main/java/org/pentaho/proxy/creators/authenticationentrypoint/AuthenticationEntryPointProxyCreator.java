/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */


package org.pentaho.proxy.creators.authenticationentrypoint;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AuthenticationEntryPoint;

public class AuthenticationEntryPointProxyCreator implements IProxyCreator<AuthenticationEntryPoint> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public boolean supports( Class aClass ) {
    // supports spring.security 3.1.4 AuthenticationEntryPoint
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.web.AuthenticationEntryPoint", aClass );
  }

  @Override
  public AuthenticationEntryPoint create( Object o ) {
    return new ProxyAuthenticationEntryPoint( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyAuthenticationEntryPoint extends ProxyObjectBase implements AuthenticationEntryPoint {

    private Method commenceMethod;

    public ProxyAuthenticationEntryPoint( Object target ) {
      super( target );
    }

    @Override
    public void commence( ServletRequest request, ServletResponse response, AuthenticationException aex )
      throws IOException, ServletException {

      try {

        Object aexProxy = getProxyFactory().createProxy( aex );

        if ( commenceMethod == null ) {

          commenceMethod =
              ProxyUtils.findMethodByName( baseTarget.getClass(), "commence",
                  javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class, aexProxy
                      .getClass() );
        }

        commenceMethod.invoke( baseTarget, (javax.servlet.http.HttpServletRequest) request,
            (javax.servlet.http.HttpServletResponse) response, aexProxy );

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage(), e );
      }
    }
  }
}
