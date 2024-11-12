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
 * Created by nbaker on 10/3/14.
 */
public class Wrap {
  String start;
  String end;

  String startFile;
  String endFile;

  public String getStartFile() {
    return startFile;
  }

  public void setStartFile( String startFile ) {
    this.startFile = startFile;
  }

  public String getEndFile() {
    return endFile;
  }

  public void setEndFile( String endFile ) {
    this.endFile = endFile;
  }

  public String getStart() {
    return start;
  }

  public void setStart( String start ) {
    this.start = start;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd( String end ) {
    this.end = end;
  }
}
