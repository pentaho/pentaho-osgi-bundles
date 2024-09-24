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
public class CompressorModule {
  String name;
  String[] include;
  String[] exclude;
  String[] excludeShallow;
  boolean create;
  String[] insertRequire;

  // Override option is a subset of the main config. We'll ignore for now.


  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String[] getInclude() {
    return include;
  }

  public void setInclude( String[] include ) {
    this.include = include;
  }

  public boolean isCreate() {
    return create;
  }

  public void setCreate( boolean create ) {
    this.create = create;
  }

  public String[] getExclude() {
    return exclude;
  }

  public void setExclude( String[] exclude ) {
    this.exclude = exclude;
  }

  public String[] getExcludeShallow() {
    return excludeShallow;
  }

  public void setExcludeShallow( String[] excludeShallow ) {
    this.excludeShallow = excludeShallow;
  }

  public String[] getInsertRequire() {
    return insertRequire;
  }

  public void setInsertRequire( String[] insertRequire ) {
    this.insertRequire = insertRequire;
  }


}
