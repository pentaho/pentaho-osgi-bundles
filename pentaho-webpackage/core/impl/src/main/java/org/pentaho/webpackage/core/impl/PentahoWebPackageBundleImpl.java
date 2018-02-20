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
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.PentahoWebPackageBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PentahoWebPackageBundleImpl implements PentahoWebPackageBundle {
  private static Logger logger = LoggerFactory.getLogger( PentahoWebPackageBundleImpl.class );

  private static final JSONParser parser = new JSONParser();

  private final Bundle bundle;

  private ArrayList<PentahoWebPackageImpl> pentahoWebPackages;

  PentahoWebPackageBundleImpl( Bundle bundle ) {
    this.bundle = bundle;

    this.pentahoWebPackages = new ArrayList<>();
  }

  private static Map<String, Object> parsePackageJson( URL resourceUrl ) throws IOException, ParseException {
    URLConnection urlConnection = resourceUrl.openConnection();
    InputStream inputStream = urlConnection.getInputStream();

    InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
    BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

    return (Map<String, Object>) parser.parse( bufferedReader );
  }

  public void init() {
    BundleWiring wiring = this.bundle.adapt( BundleWiring.class );

    if ( wiring != null ) {
      List<BundleCapability> capabilities = wiring.getCapabilities( PentahoWebPackageBundle.CAPABILITY_NAMESPACE );
      capabilities.forEach( bundleCapability -> {
        Map<String, Object> attributes = bundleCapability.getAttributes();

        // for now using only the package.json information - so only the `root` attribute is mandatory
//        String name = (String) attributes.get( "name" );
//        Version version = (Version) attributes.get( "version" );
        String root = (String) attributes.getOrDefault( "root", "" );
        while ( root.endsWith( "/" ) ) {
          root = root.substring( 0, root.length() - 1 );
        }

        try {
          URL capabilityPackageJsonUrl = this.bundle.getResource( root + "/package.json" );
          if ( capabilityPackageJsonUrl != null ) {
            Map<String, Object> packageJson = parsePackageJson( capabilityPackageJsonUrl );

            String name = (String) packageJson.get( "name" );
            String version = (String) packageJson.get( "version" );

            if ( name != null && version != null ) {
              this.pentahoWebPackages.add( new PentahoWebPackageImpl( this.bundle, name, version, ( root.isEmpty() ? "/" : root ) ) );
            }
          } else {
            logger.warn( this.bundle.getSymbolicName() + " [" + this.bundle.getBundleId() + "]: " + root + "/package.json not found." );
          }
        } catch ( RuntimeException | ParseException | IOException ignored ) {
          logger.error( this.bundle.getSymbolicName() + " [" + this.bundle.getBundleId() + "]: Error parsing " + root + "/package.json." );

          // throwing will make everything fail
          // what damage control should we do?
          // **don't register this capability?** <-- this is what we're doing now
          // ignore and use only the capability info?
          // don't register all the bundle's capabilities?
          // this is all post-bundle wiring phase, so only the requirejs configuration is affected
          // the bundle is started and nothing will change that... or should we bundle.stop()?
        }
      } );

      this.pentahoWebPackages.forEach( PentahoWebPackageImpl::init );
    }
  }

  public void destroy() {
    this.pentahoWebPackages.forEach( PentahoWebPackageImpl::destroy );
  }

}
