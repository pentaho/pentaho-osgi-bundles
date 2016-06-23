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

package org.pentaho.proxy.creators;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyFactory;

import java.lang.reflect.Method;

public class ProxyUtils {

  private static ProxyUtils singleton;

  private IProxyFactory proxyFactory;

  protected ProxyUtils() {
    proxyFactory = PentahoSystem.get( IProxyFactory.class );
  }

  public static ProxyUtils getInstance() {
    if ( singleton == null ) {
      singleton = new ProxyUtils();
    }
    return singleton;
  }

  // instance methods

  public IProxyFactory getProxyFactory() {
    return proxyFactory;
  }

  // static methods

  public static Method findMethodByName( Class clazz, String methodName ) {
    return findMethodByName( clazz, methodName, new Class[] {} /* zero-args method */ );
  }

  public static Method findMethodByName( Class clazz, String methodName, Class... arguments ) {
    for ( Method method : clazz.getMethods() ) {
      if ( method.getName().equals( methodName ) ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          if ( method.getParameterTypes()[ i ].isAssignableFrom( arguments[i] ) == false ) {
            return null;
          }
        }
        return method;
      }
    }
    return null;
  }

  public static boolean isRecursivelySupported( final String supportedClassName, Class aClass  ) {

    if ( aClass != null && supportedClassName != null && !supportedClassName.isEmpty() ) {

      // 1. the class itself is the 'supportedClassName'
      if ( supportedClassName.equals( aClass.getName() ) ) {
        return true;

      }

      // 2. one of its interfaces is the 'supportedClassName'
      if ( aClass.getInterfaces().length > 0 ) {

        for ( Class c : aClass.getInterfaces() ) {

          if ( c.getName().equals( supportedClassName ) ) {
            return true;
          }
        }
      }

      // 3. one of its superclasses is the 'supportedClassName' ( recursive check )
      if ( aClass.getSuperclass() != null ) {
        return isRecursivelySupported( supportedClassName, aClass.getSuperclass() );
      }

    }

    // nope
    return false;
  }
}
