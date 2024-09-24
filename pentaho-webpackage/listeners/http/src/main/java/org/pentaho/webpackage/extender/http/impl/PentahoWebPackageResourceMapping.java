/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
