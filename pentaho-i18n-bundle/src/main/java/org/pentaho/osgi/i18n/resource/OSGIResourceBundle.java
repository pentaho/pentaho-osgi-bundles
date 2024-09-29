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
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/4/14.
 */
public class OSGIResourceBundle extends PropertyResourceBundle {
  private final String defaultName;

  public OSGIResourceBundle( String defaultName, URL propertyFileUrl ) throws IOException {
    this( defaultName, null, propertyFileUrl );
  }

  public OSGIResourceBundle( String defaultName, ResourceBundle parent, URL propertyFileUrl ) throws IOException {
    super( propertyFileUrl.openStream() );
    this.defaultName = defaultName;
    if ( parent != null ) {
      setParent( parent );
    }
  }

  public String getDefaultName() {
    return defaultName;
  }

  public ResourceBundle getParent() {
    return parent;
  }
}
