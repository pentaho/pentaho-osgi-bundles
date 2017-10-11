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
 *
 * Copyright 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.webpackage.extender.http.impl;

import org.pentaho.webpackage.core.PentahoWebPackage;

public abstract class PentahoWebPackageAbstract implements PentahoWebPackage {
  private final String name;
  private final String version;
  private final String resourceRootPath;

  PentahoWebPackageAbstract( String name, String version, String resourceRootPath ) {
    this.name = name;
    this.version = version;
    this.resourceRootPath = resourceRootPath;
  }

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }

  public String getResourceRootPath() {
    return this.resourceRootPath;
  }
}
