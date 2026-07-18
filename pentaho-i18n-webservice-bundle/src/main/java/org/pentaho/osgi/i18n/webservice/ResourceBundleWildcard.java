/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.osgi.i18n.webservice;

/**
 * Created by bryan on 12/8/14.
 */
public class ResourceBundleWildcard {
  private String keyRegex;

  public String getKeyRegex() {
    return keyRegex;
  }

  public void setKeyRegex( String keyRegex ) {
    this.keyRegex = keyRegex;
  }
}
