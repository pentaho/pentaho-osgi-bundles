/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.proxy.creators.userdetailsservice;

import org.pentaho.proxy.creators.ProxyObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyUserDetailsService extends ProxyObjectBase implements UserDetailsService {
  private Logger logger = LoggerFactory.getLogger( getClass() );

  private String FULL_NAME_SS4_USERNOTFOUNDEXCEPTION = "org.springframework.security.core.userdetails.UsernameNotFoundException";

  Method loadUserByNameMethod;
  Class<?> loadUserByNameReturnType;

  public ProxyUserDetailsService( Object sourceObject ) {
    super(sourceObject);
    Class<? extends Object> clazz = sourceObject.getClass();
    loadUserByNameMethod = ReflectionUtils.findMethod( clazz, "loadUserByUsername", new Class[] { String.class } );
    loadUserByNameReturnType = loadUserByNameMethod.getReturnType();
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    try {
      Object result = ReflectionUtils.invokeMethod( loadUserByNameMethod, baseTarget, new Object[] { username } );
      if ( result != null ) {
        return new ProxyUserDetails( result );
      } else {
        logger.warn( "Got a null from calling the method loadUserByUsername( String username ) of UserDetailsService: "
            + baseTarget
            + ". This is an interface violation beacuse it is specified that loadUserByUsername method should never return null. Throwing a UsernameNotFoundException." );
      }
    } catch ( Exception e ) {
      if ( e.getClass().getName().equals( FULL_NAME_SS4_USERNOTFOUNDEXCEPTION ) ) {
        throw new UsernameNotFoundException( username + " not found", e );
      } else {
        logger.error( e.getMessage(), e );
      }
    }
    throw new UsernameNotFoundException( username );
  }
}
