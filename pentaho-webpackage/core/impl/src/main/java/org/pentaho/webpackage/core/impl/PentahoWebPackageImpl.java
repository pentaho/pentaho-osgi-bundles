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
