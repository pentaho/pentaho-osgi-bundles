/*! ******************************************************************************
 *
 * Pentaho OSGi Bundles
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.proxy.creators;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyObjectBase {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  protected Object baseTarget;
  private Method toString;

  // TODO: Consider implementing equals and hashCode
  /*
   * private Method equals; private Method hashCode;
   */

  protected ProxyObjectBase( Object baseTarget ) {
    this.baseTarget = baseTarget;
  }

  /*
   * Note: Left implementation open so objects extending can get toString without proxy object reference
   */
  public String baseTargetToString() {
    try {
      if ( toString == null ) {
        toString = ProxyUtils.findMethodByName( baseTarget.getClass(), "toString" );
      }

      return (String) toString.invoke( baseTarget );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      logger.error( e.getMessage(), e );
    }
    return super.toString();
  }

  @Override
  public String toString() {
    return super.toString() + " " + baseTargetToString();
  }

}
