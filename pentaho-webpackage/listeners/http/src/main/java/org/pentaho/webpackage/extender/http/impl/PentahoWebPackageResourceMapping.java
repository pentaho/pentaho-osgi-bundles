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

package org.pentaho.webpackage.extender.http.impl;

import org.pentaho.webpackage.core.IPentahoWebPackage;

public class PentahoWebPackageResourceMapping {
  private final IPentahoWebPackage pentahoWebPackage;

  public PentahoWebPackageResourceMapping( IPentahoWebPackage pentahoWebPackage ) {
    super();

    this.pentahoWebPackage = pentahoWebPackage;
  }

  public String getAlias() {
    return this.pentahoWebPackage.getWebRootPath();
  }

  public String getPath() {
    String resourceRootPath = this.pentahoWebPackage.getResourceRootPath();
    while ( resourceRootPath.length() > 1 && resourceRootPath.endsWith( "/" ) ) {
      resourceRootPath = resourceRootPath.substring( 0, resourceRootPath.length() - 1 );
    }

    return resourceRootPath;
  }

  public String toString() {
    return this.getClass().getSimpleName() + "{" + "alias=" + this.getAlias() + ",path=" + this.getPath() + "}";
  }

  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    } else if ( obj == null ) {
      return false;
    } else if ( this.getClass() != obj.getClass() ) {
      return false;
    } else {
      PentahoWebPackageResourceMapping other = (PentahoWebPackageResourceMapping) obj;

      return other.pentahoWebPackage.equals( this.pentahoWebPackage );
    }
  }
}
