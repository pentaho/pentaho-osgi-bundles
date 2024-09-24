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

import java.util.HashMap;

/**
 * Created by nbaker on 10/3/14.
 */
public class Uglify2Config {
  public class Output{
    boolean beautify;

    public boolean isBeautify() {
      return beautify;
    }

    public void setBeautify( boolean beautify ) {
      this.beautify = beautify;
    }
  }
  public class Compress{
    boolean sequences;
    HashMap<String, Object> globalDefs;

    public boolean isSequences() {
      return sequences;
    }

    public void setSequences( boolean sequences ) {
      this.sequences = sequences;
    }

    public HashMap<String, Object> getGlobalDefs() {
      return globalDefs;
    }

    public void setGlobalDefs( HashMap<String, Object> globalDefs ) {
      this.globalDefs = globalDefs;
    }
  }
  Output output;
  Compress compress;
  boolean warnings;
  boolean mangle;

  public Output getOutput() {
    return output;
  }

  public void setOutput( Output output ) {
    this.output = output;
  }

  public Compress getCompress() {
    return compress;
  }

  public void setCompress( Compress compress ) {
    this.compress = compress;
  }

  public boolean isWarnings() {
    return warnings;
  }

  public void setWarnings( boolean warnings ) {
    this.warnings = warnings;
  }

  public boolean isMangle() {
    return mangle;
  }

  public void setMangle( boolean mangle ) {
    this.mangle = mangle;
  }



}
