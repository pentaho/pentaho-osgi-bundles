/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.osgi.requirejs.compressor.bindings;

/**
 * Created by nbaker on 10/3/14.
 */
public class ThrowWhen {
  boolean optimize;

  public boolean isOptimize() {
    return optimize;
  }

  public void setOptimize( boolean optimize ) {
    this.optimize = optimize;
  }
}
