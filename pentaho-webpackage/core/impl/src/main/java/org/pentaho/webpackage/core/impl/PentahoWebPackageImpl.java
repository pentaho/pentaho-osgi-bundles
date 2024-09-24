/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

public final class PentahoWebPackageImpl implements IPentahoWebPackage {
  private final String name;
  private final String version;
  private final String resourceRootPath;
  private final URL packageJsonUrl;

  public PentahoWebPackageImpl( String resourceRootPath, URL packageJsonUrl ) {
    this.resourceRootPath = resourceRootPath;
    this.packageJsonUrl = packageJsonUrl;

    // caching name and version
    Map<String, Object> packageJson = this.getPackageJson();
    this.name = (String) packageJson.get( "name" );
    this.version = (String) packageJson.get( "version" );

    if ( this.name == null || this.version == null ) {
      throw new java.lang.IllegalArgumentException( "Cannot create WebPackage with null Name or Version." );
    }
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

  @Override
  public String getWebRootPath() {
    return "/" + this.getName() + "@" + this.getVersion();
  }

  @Override
  public Map<String, Object> getPackageJson() {
    try {
      URLConnection urlConnection = this.packageJsonUrl.openConnection();
      urlConnection.connect();
      InputStream inputStream = urlConnection.getInputStream();

      InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
      BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

      return (Map<String, Object>) (new JSONParser()).parse( bufferedReader );
    } catch ( IOException | ParseException ignored ) {
    }

    return Collections.emptyMap();
  }

}
