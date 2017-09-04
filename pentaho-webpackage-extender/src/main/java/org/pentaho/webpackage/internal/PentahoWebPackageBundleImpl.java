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
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.webpackage.internal;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.PentahoWebPackage;
import org.pentaho.webpackage.PentahoWebPackageBundle;
import org.pentaho.webpackage.PentahoWebPackageService;

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
  private static final JSONParser parser = new JSONParser();

  private final Bundle bundle;

  private ArrayList<PentahoWebPackage> pentahoWebPackages;

  PentahoWebPackageBundleImpl( Bundle bundle ) {
    this.bundle = bundle;

    this.pentahoWebPackages = new ArrayList<>();
  }

  private static Map<String, Object> parsePackageJson( URL resourceUrl ) throws IOException, ParseException {
    URLConnection urlConnection = resourceUrl.openConnection();
    InputStream inputStream = urlConnection.getInputStream();

    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;

    try {
      inputStreamReader = new InputStreamReader( inputStream );
      bufferedReader = new BufferedReader( inputStreamReader );

      return (Map<String, Object>) parser.parse( bufferedReader );
    } catch ( Exception ignored ) {
      // ignored
    }

    return null;
  }

  @Override
  public void init() {
    BundleWiring wiring = this.bundle.adapt( BundleWiring.class );

    if ( wiring != null ) {
      List<BundleCapability> capabilities = wiring.getCapabilities( PentahoWebPackageService.CAPABILITY_NAMESPACE );
      capabilities.forEach( bundleCapability -> {
        Map<String, Object> attributes = bundleCapability.getAttributes();

        // for now using only the package.json information - so only the `root` attribute is mandatory
//        String name = (String) attributes.get( "name" );
//        Version version = (Version) attributes.get( "version" );
        String root = (String) attributes.getOrDefault( "root", "" );

        try {
          URL capabilityPackageJsonUrl = this.bundle.getResource( root + "/package.json" );
          if ( capabilityPackageJsonUrl != null ) {
            Map<String, Object> packageJson = parsePackageJson( capabilityPackageJsonUrl );

            String name = (String) packageJson.get( "name" );
            String version = (String) packageJson.get( "version" );

            if ( name != null && version != null ) {
              this.pentahoWebPackages.add( new PentahoWebPackageImpl( this.bundle, name, version, root ) );
            }
          }
        } catch ( RuntimeException | ParseException | IOException ignored ) {
          // throwing will make everything fail
          // what damage control should we do?
          // **don't register this capability?** <-- this is what we're doing now
          // ignore and use only the capability info?
          // don't register all the bundle's capabilities?
          // this is all post-bundle wiring phase, so only the requirejs configuration is affected
          // the bundle is started and nothing will change that... or should we bundle.stop()?
        }
      } );

      this.pentahoWebPackages.forEach( PentahoWebPackage::init );
    }
  }

  @Override
  public void destroy() {
    this.pentahoWebPackages.forEach( PentahoWebPackage::destroy );
  }

}
