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


package org.pentaho.proxy.creators.grantedauthorities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public class S4GrantedAuthorityProxyCreator implements IProxyCreator<GrantedAuthority> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public boolean supports( Class aClass ) {
    // supports legacy spring.security 2.0.8 SecurityContext
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.GrantedAuthority", aClass );
  }

  @Override
  public GrantedAuthority create( Object o ) {
    return new ProxyGrantedAuthority( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyGrantedAuthority extends ProxyObjectBase implements GrantedAuthority {

    /**
     *
     */
    private static final long serialVersionUID = 1603112902745163281L;

    private Method getAuthorityMethod;

    public ProxyGrantedAuthority( Object target ) {
      super( target );
    }

    @Override
    public String getAuthority() {
      try {

        if ( getAuthorityMethod == null ) {
          getAuthorityMethod = ProxyUtils.findMethodByName( baseTarget.getClass(), "getAuthority" );
        }

        return (String) getAuthorityMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }

  }

}
