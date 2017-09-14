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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.notification.api.match;


import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.MatchConditionException;

import java.lang.reflect.Method;

/**
 * Created by bryan on 9/22/14.
 */
@SuppressWarnings( "unchecked" )
public class BeanPropertyCondition implements MatchCondition {
  private final Class<?> clazz;
  private final Method getter;
  private final MatchCondition matchCondition;

  public BeanPropertyCondition( Class<?> clazz, String propertyName, MatchCondition matchCondition )
    throws MatchConditionException {
    this.clazz = clazz;
    StringBuilder firstUpperCasedPropertyNameSb = new StringBuilder( propertyName.substring( 0, 1 ).toUpperCase() );
    if ( propertyName.length() > 1 ) {
      firstUpperCasedPropertyNameSb.append( propertyName.substring( 1 ) );
    }
    String firstUpperCasedPropertyName = firstUpperCasedPropertyNameSb.toString();
    Method getter = null;
    try {
      getter = clazz.getMethod( "get" + firstUpperCasedPropertyName );
    } catch ( NoSuchMethodException e ) {
      try {
        getter = clazz.getMethod( "is" + firstUpperCasedPropertyName );
      } catch ( NoSuchMethodException e1 ) {
        throw new MatchConditionException( e1 );
      }
    }
    this.getter = getter;
    this.matchCondition = matchCondition;
  }

  @Override public boolean matches( Object object ) {
    if ( !clazz.isInstance( object ) ) {
      return false;
    }
    Object val = null;
    try {
      val = getter.invoke( object );
    } catch ( Exception e ) {
      return false;
    }
    return matchCondition.matches( val );
  }
}
