/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
