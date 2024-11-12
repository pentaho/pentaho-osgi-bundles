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


package org.pentaho.platform.osgi.requirejs.compressor.bindings;

import java.util.HashMap;

/**
 * Created by nbaker on 10/3/14.
 */
public class Closure {

  HashMap<String, Object> compilerOptions;
  String compilationLevel;
  String loggingLevel;

  public HashMap<String, Object> getCompilerOptions() {
    return compilerOptions;
  }

  public void setCompilerOptions( HashMap<String, Object> compilerOptions ) {
    this.compilerOptions = compilerOptions;
  }

  public String getCompilationLevel() {
    return compilationLevel;
  }

  public void setCompilationLevel( String compilationLevel ) {
    this.compilationLevel = compilationLevel;
  }

  public String getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel( String loggingLevel ) {
    this.loggingLevel = loggingLevel;
  }
}
