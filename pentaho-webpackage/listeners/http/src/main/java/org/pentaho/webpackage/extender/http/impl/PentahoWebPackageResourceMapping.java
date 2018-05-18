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

import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.pentaho.webpackage.core.IPentahoWebPackage;

/**
 * A webpackage targeted implementation of {@link ResourceMapping}.
 */
public class PentahoWebPackageResourceMapping implements ResourceMapping {
  private final IPentahoWebPackage pentahoWebPackage;

  public PentahoWebPackageResourceMapping( IPentahoWebPackage pentahoWebPackage ) {
    super();

    this.pentahoWebPackage = pentahoWebPackage;
  }

  @Override
  public String getHttpContextId() {
    return null;
  }

  @Override
  public String getAlias() {
    return this.pentahoWebPackage.getWebRootPath();
  }

  @Override
  public String getPath() {
    return this.pentahoWebPackage.getResourceRootPath();
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
