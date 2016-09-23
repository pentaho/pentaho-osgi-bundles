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

import java.lang.reflect.Method;

import org.pentaho.proxy.creators.ProxyObjectBase;
import org.springframework.security.GrantedAuthority;
import org.springframework.util.ReflectionUtils;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyGrantedAuthority extends ProxyObjectBase implements GrantedAuthority {
  private static final long serialVersionUID = 1L;
  Method getAuthorityMethod;
  Method compareToMethod;

  public ProxyGrantedAuthority( Object sourceObject ) {
    super( sourceObject );
    Class<? extends Object> clazz = sourceObject.getClass();
    getAuthorityMethod = ReflectionUtils.findMethod( clazz, "getAuthority" );
    compareToMethod = ReflectionUtils.findMethod( clazz, "compareTo", new Class[] { Object.class } );
  }

  @Override
  public int compareTo( Object o ) {
    if ( o != null && o instanceof GrantedAuthority ) {
      String rhsRole = ( (GrantedAuthority) o ).getAuthority();

      if ( rhsRole == null ) {
        return -1;
      }
      return getAuthority().compareTo( rhsRole );
    }
    return -1;
  }

  @Override
  public String getAuthority() {
    return (String) ReflectionUtils.invokeMethod( getAuthorityMethod, baseTarget );
  }

}
