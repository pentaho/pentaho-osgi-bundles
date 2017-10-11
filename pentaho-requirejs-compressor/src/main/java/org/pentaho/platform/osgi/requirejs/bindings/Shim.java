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
 * Copyright 2014 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.osgi.requirejs.bindings;

import java.util.Arrays;

/**
 * Created by nbaker on 10/1/14.
 */
public class Shim {
  String[] deps;
  String exports;

  public String[] getDeps() {
    return deps;
  }

  public void setDeps( String[] deps ) {
    this.deps = deps;
  }

  public String getExports() {
    return exports;
  }

  public void setExports( String exports ) {
    this.exports = exports;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof Shim ) ) {
      return false;
    }

    Shim shim = (Shim) o;

    if ( !Arrays.equals( deps, shim.deps ) ) {
      return false;
    }
    if ( exports != null ? !exports.equals( shim.exports ) : shim.exports != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = deps != null ? Arrays.hashCode( deps ) : 0;
    result = 31 * result + ( exports != null ? exports.hashCode() : 0 );
    return result;
  }
}
