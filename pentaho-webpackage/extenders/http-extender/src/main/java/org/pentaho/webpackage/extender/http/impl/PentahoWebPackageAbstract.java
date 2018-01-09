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
