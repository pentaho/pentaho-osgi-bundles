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

package org.pentaho.proxy.creators.authenticationprovider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ReflectionUtils;

/**
 * Created by nbaker on 8/31/15.
 */
public class S4AuthenticationProxyCreator implements IProxyCreator<Authentication> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  private IProxyFactory iProxyFactory;

  @Override
  public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.Authentication", aClass );
  }

  @Override
  public Authentication create( Object o ) {
    String className = o.getClass().getName();
    if ( "org.springframework.security.providers.UsernamePasswordAuthenticationToken".equals( className ) ) {
      Method getCredentials = ReflectionUtils.findMethod( o.getClass(), "getCredentials" );
      Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal" );

      try {
        Object credentials = getCredentials.invoke( o, new Object[] {} );
        Object principal = getPrincipal.invoke( o, new Object[] {} );
        return new UsernamePasswordAuthenticationToken( principal, credentials );
      } catch ( IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      } catch ( InvocationTargetException e ) {
        logger.error( e.getMessage(), e );
      }
    } else if ( "org.springframework.security.providers.anonymous.AnonymousAuthenticationToken".equals( className ) ) {

      Method getKeyHash = ReflectionUtils.findMethod( o.getClass(), "getKeyHash" );
      Method getDetails = ReflectionUtils.findMethod( o.getClass(), "getDetails" );
      Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal" );
      Method getAuthorities = ReflectionUtils.findMethod( o.getClass(), "getAuthorities" );

      try {

        getKeyHash.setAccessible( true );
        getDetails.setAccessible( true );
        getPrincipal.setAccessible( true );
        getAuthorities.setAccessible( true );

        Object keyHash = getKeyHash.invoke( o );
        Object details = getDetails.invoke( o );
        Object principal = getPrincipal.invoke( o );
        Object authoritiesObj = getAuthorities.invoke( o );

        Collection<SimpleGrantedAuthority> s4Authorities = new ArrayList<SimpleGrantedAuthority>();

        if ( authoritiesObj != null && authoritiesObj instanceof Object[] ) {

          for ( Object authorityObj : (Object[]) authoritiesObj ) {

            Method getAuthority = ReflectionUtils.findMethod( authorityObj.getClass(), "getAuthority" );
            Object authority = getAuthority.invoke( authorityObj );
            if ( authority != null ) {
              s4Authorities.add( new SimpleGrantedAuthority( authority.toString() ) );
            }
          }
        }

        AnonymousAuthenticationToken anonymousToken =
            new AnonymousAuthenticationToken( keyHash.toString(), principal, s4Authorities );
        anonymousToken.setDetails( details );

        return anonymousToken;

      } catch ( IllegalAccessException | InvocationTargetException e ) {
        logger.error( e.getMessage(), e );
      }

    }

    return null;
  }

  private IProxyFactory getFactory() {
    if ( iProxyFactory == null ) {
      iProxyFactory = PentahoSystem.get( IProxyFactory.class );
    }
    return iProxyFactory;
  }

}
