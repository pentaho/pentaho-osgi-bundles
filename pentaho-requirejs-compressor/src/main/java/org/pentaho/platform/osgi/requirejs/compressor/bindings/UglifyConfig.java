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

/**
 * Created by nbaker on 10/2/14.
 */
public class UglifyConfig {

  boolean toplevel;
  boolean asciiOnly;
  boolean beautify;
  int maxLineLength;
  boolean no_mangle;

  public boolean isToplevel() {
    return toplevel;
  }

  public void setToplevel( boolean toplevel ) {
    this.toplevel = toplevel;
  }

  public boolean isAsciiOnly() {
    return asciiOnly;
  }

  public void setAsciiOnly( boolean asciiOnly ) {
    this.asciiOnly = asciiOnly;
  }

  public boolean isBeautify() {
    return beautify;
  }

  public void setBeautify( boolean beautify ) {
    this.beautify = beautify;
  }

  public int getMaxLineLength() {
    return maxLineLength;
  }

  public void setMaxLineLength( int maxLineLength ) {
    this.maxLineLength = maxLineLength;
  }

  public boolean isNo_mangle() {
    return no_mangle;
  }

  public void setNo_mangle( boolean no_mangle ) {
    this.no_mangle = no_mangle;
  }
}
