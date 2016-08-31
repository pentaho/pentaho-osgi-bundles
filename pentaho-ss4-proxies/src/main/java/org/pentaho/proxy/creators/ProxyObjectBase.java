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
