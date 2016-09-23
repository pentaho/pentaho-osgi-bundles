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


package org.pentaho.proxy.creators.securitycontext;

import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SecurityContextProxyCreator implements IProxyCreator<SecurityContext> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public boolean supports( Class aClass ) {
    // supports spring.security 3.1.4 SecurityContext
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.core.context.SecurityContext", aClass );
  }

  @Override
  public SecurityContext create( Object o ) {
    return new ProxySecurityContext( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxySecurityContext extends ProxyObjectBase implements SecurityContext {

    private Method getAuthenticationMethod;
    private Method setAuthenticationMethod;

    public ProxySecurityContext( Object target ) {
      super( target );
    }

    @Override
    public Authentication getAuthentication() {

      try {

        if ( getAuthenticationMethod == null ) {
          getAuthenticationMethod = ProxyUtils.findMethodByName( baseTarget.getClass(), "getAuthentication" );
        }

        Object retVal = getAuthenticationMethod.invoke( baseTarget );

        if ( retVal != null ) {
          return getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }

    @Override
    public void setAuthentication( Authentication authentication ) {

      try {

        if ( authentication == null ) {

          setAuthenticationMethod = ProxyUtils.findMethodByName( baseTarget.getClass(), "setAuthentication" );
          setAuthenticationMethod.invoke( baseTarget, null );

        } else {

          Object auth = ProxyUtils.getInstance().getProxyFactory().createProxy( authentication );
          setAuthenticationMethod =
              ProxyUtils.findMethodByName( baseTarget.getClass(), "setAuthentication", auth.getClass() );

          setAuthenticationMethod.invoke( baseTarget, auth );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage(), e );
      }
    }
  }

}
