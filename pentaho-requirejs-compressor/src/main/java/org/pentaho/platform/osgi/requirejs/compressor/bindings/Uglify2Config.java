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
