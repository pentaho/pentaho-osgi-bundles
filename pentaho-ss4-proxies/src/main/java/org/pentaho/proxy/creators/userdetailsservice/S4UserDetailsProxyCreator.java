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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

public class S4UserDetailsProxyCreator implements IProxyCreator<UserDetails> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public boolean supports( Class<?> clazz ) {
    // supports legacy spring.security 2.0.8 UserDetails
    return "org.springframework.security.userdetails.UserDetails".equals( clazz.getName() );
  }

  @Override
  public UserDetails create( Object target ) {
    return new S4UserDetailsProxy( target );
  }

  private class S4UserDetailsProxy extends ProxyObjectBase implements UserDetails {

    private Method getAuthoritiesMethod;
    private Method getPasswordMethod;
    private Method getUsernameMethod;
    private Method isAccountNonExpiredMethod;
    private Method isAccountNonLockedMethod;
    private Method isCredentialsNonExpiredMethod;
    private Method isEnabledMethod;

    public S4UserDetailsProxy( Object target ) {
      super( target );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

      if ( getAuthoritiesMethod == null ) {
        getAuthoritiesMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "getAuthorities" );
      }

      Collection<SimpleGrantedAuthority> s4Authorities = new ArrayList<SimpleGrantedAuthority>();

      try {

        Object authoritiesObj = getAuthoritiesMethod.invoke( baseTarget );

        if ( authoritiesObj != null && authoritiesObj instanceof Object[] ) {

          for ( Object authorityObj : (Object[]) authoritiesObj ) {

            Method getAuthority = ReflectionUtils.findMethod( authorityObj.getClass(), "getAuthority" );
            Object authority = getAuthority.invoke( authorityObj );
            if ( authority != null ) {
              s4Authorities.add( new SimpleGrantedAuthority( authority.toString() ) );
            }
          }
        }

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return s4Authorities;
    }

    @Override
    public String getPassword() {

      if ( getPasswordMethod == null ) {
        getPasswordMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "getPassword" );
      }

      try {

        return (String) getPasswordMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }

    @Override
    public String getUsername() {

      if ( getUsernameMethod == null ) {
        getUsernameMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "getUsername" );
      }

      try {

        return (String) getUsernameMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }

    @Override
    public boolean isAccountNonExpired() {

      if ( isAccountNonExpiredMethod == null ) {
        isAccountNonExpiredMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "isAccountNonExpired" );
      }

      try {

        return (Boolean) isAccountNonExpiredMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return false;
    }

    @Override
    public boolean isAccountNonLocked() {

      if ( isAccountNonLockedMethod == null ) {
        isAccountNonLockedMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "isAccountNonLocked" );
      }

      try {

        return (Boolean) isAccountNonLockedMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {

      if ( isCredentialsNonExpiredMethod == null ) {
        isCredentialsNonExpiredMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "isCredentialsNonExpired" );
      }

      try {

        return (Boolean) isCredentialsNonExpiredMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return false;
    }

    @Override
    public boolean isEnabled() {

      if ( isEnabledMethod == null ) {
        isEnabledMethod = ReflectionUtils.findMethod( baseTarget.getClass(), "isEnabled" );
      }

      try {

        return (Boolean) isEnabledMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      }

      return false;
    }
  }
}
