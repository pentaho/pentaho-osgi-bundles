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


package org.pentaho.proxy.creators.userdetailsservice;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4UserDetailsServiceProxyCreator implements IProxyCreator<UserDetailsService> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  private String FULL_NAME_SS2_USERNOTFOUNDEXCEPTION =
      "org.springframework.security.userdetails.UsernameNotFoundException";

  @Override
  public boolean supports( Class<?> clazz ) {
    // supports legacy spring.security 2.0.8 UserDetailsService
    return "org.springframework.security.userdetails.UserDetailsService".equals( clazz.getName() );
  }

  @Override
  public UserDetailsService create( Object target ) {
    return new S4UserDetailsServiceProxy( target );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class S4UserDetailsServiceProxy implements UserDetailsService {

    private Object target;

    private Method loadUserByUsernameMethod;

    public S4UserDetailsServiceProxy( Object target ) {
      this.target = target;
    }

    @Override
    public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {

      if ( loadUserByUsernameMethod == null ) {
        loadUserByUsernameMethod = ReflectionUtils.findMethod( target.getClass(), "loadUserByUsername", String.class );
      }

      try {

        Object retVal = loadUserByUsernameMethod.invoke( target, username );

        if ( retVal != null ) {
          return getProxyFactory().createProxy( retVal );
        } else {
          logger.warn(
              "Got a null from calling the method loadUserByUsername( String username ) of UserDetailsService: "
                  + target
                  + ". This is an interface violation beacuse it is specified that loadUserByUsername method should never return null. Throwing a UsernameNotFoundException." );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        if ( e.getCause() != null && e.getCause().getClass().getName().equals( FULL_NAME_SS2_USERNOTFOUNDEXCEPTION ) ) {
          throw new UsernameNotFoundException( e.getCause().getMessage(), e );
        }
        logger.error( e.getMessage(), e );
      }

      throw new UsernameNotFoundException( username );
    }
  }
}
