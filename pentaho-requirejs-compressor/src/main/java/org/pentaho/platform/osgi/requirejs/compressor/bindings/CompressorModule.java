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
