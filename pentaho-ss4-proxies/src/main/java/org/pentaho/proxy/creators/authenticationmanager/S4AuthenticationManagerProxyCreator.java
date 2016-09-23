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

package org.pentaho.proxy.creators.authenticationmanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class S4AuthenticationManagerProxyCreator implements IProxyCreator<AuthenticationManager> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.AuthenticationManager", aClass );
  }

  @Override
  public AuthenticationManager create( Object o ) {
    return new ProxyAuthenticationManager( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyAuthenticationManager extends ProxyObjectBase implements AuthenticationManager {

    public ProxyAuthenticationManager( Object target ) {
      super( target );
    }

    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

      try {
        Object auth = getProxyFactory().createProxy( authentication );
        Method authenticate = ProxyUtils.findMethodByName( baseTarget.getClass(), "authenticate", auth.getClass() );
        Object retVal = authenticate.invoke( baseTarget, auth );

        if ( retVal != null ) {
          return getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }
  }
}
