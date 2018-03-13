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
package org.pentaho.webpackage.core.impl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.webpackage.core.PentahoWebPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class PentahoWebPackageImpl implements PentahoWebPackage {
  private final String name;
  private final String version;
  private final String resourceRootPath;

  private final BundleContext bundleContext;

  private ServiceRegistration<?> serviceReference;

  PentahoWebPackageImpl( BundleContext bundleContext, String name, String version, String resourceRootPath ) {
    this.bundleContext = bundleContext;

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

  @Override
  public String getWebRootPath() {
    return "/" + this.getName() + "/" + this.getVersion();
  }

  @Override
  public Map<String, Object> getPackageJson() {
    try {
      Bundle bundle = this.bundleContext.getBundle();
      String scriptPath = this.getResourceRootPath() + "/package.json";

      URL resourceUrl = bundle.getResource( scriptPath );
      URLConnection urlConnection = resourceUrl.openConnection();
      InputStream inputStream = urlConnection.getInputStream();

      InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
      BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

      JSONParser parser = new JSONParser();
      return (Map<String, Object>) parser.parse( bufferedReader );
    } catch ( IOException | ParseException ignored ) {
    }

    return null;
  }

  public void init() {
    this.serviceReference = this.bundleContext.registerService( PentahoWebPackage.class.getName(), this, null );
  }

  public void destroy() {
    if ( this.serviceReference != null ) {
      try {
        this.serviceReference.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.serviceReference = null;
    }
  }
}
