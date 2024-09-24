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
