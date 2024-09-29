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

package org.pentaho.osgi.i18n.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleFactory {
  private final int priority;
  private final String defaultName;
  private final String relativeName;
  private final URL propertyFileUrl;
  private ResourceBundle previousParent = null;
  private OSGIResourceBundle previousResult = null;

  public OSGIResourceBundleFactory( String defaultName, String relativeName, URL propertyFileUrl, int priority ) {
    this.defaultName = defaultName;
    this.priority = priority;
    this.relativeName = relativeName;
    this.propertyFileUrl = propertyFileUrl;
  }

  public synchronized OSGIResourceBundle getBundle( ResourceBundle parent ) throws IOException {
    if ( previousResult == null || previousParent != parent ) {
      previousParent = parent;
      previousResult = new OSGIResourceBundle( defaultName, parent, propertyFileUrl );
    }
    return previousResult;
  }

  public int getPriority() {
    return priority;
  }

  public String getPropertyFilePath() {
    return relativeName;
  }
}
