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

package org.pentaho.osgi.i18n.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OSGIResourceBundleFactory {
  private final int priority;
  private final String resourcePath;
  private final String resourceKey;
  private final URL propertyFileUrl;
  private ResourceBundle previousParent = null;
  private OSGIResourceBundle previousResult = null;

  public OSGIResourceBundleFactory( String resourceKey, String resourcePath, URL propertyFileUrl, int priority ) {
    this.resourceKey = resourceKey;
    this.resourcePath = resourcePath;
    this.propertyFileUrl = propertyFileUrl;
    this.priority = priority; // never used
  }

  public synchronized OSGIResourceBundle getBundle( ResourceBundle parent ) throws IOException {
    if ( this.previousResult == null || this.previousParent != parent ) {
      this.previousParent = parent;
      this.previousResult = new OSGIResourceBundle( this.resourceKey, parent, this.propertyFileUrl );
    }

    return this.previousResult;
  }

  public int getPriority() {
    return this.priority;
  }

  public String getPropertyFilePath() {
    return this.resourcePath;
  }
}
