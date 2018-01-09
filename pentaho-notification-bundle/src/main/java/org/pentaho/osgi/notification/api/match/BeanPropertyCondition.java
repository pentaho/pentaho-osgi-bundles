/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.notification.api.match;


import org.pentaho.osgi.notification.api.MatchCondition;
import org.pentaho.osgi.notification.api.MatchConditionException;

import java.lang.reflect.Method;

/**
 * Created by bryan on 9/22/14.
 */
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
