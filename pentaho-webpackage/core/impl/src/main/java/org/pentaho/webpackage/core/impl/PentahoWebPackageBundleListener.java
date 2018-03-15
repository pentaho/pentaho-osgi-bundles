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
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pentaho.webpackage.core.PentahoWebPackageConstants.CAPABILITY_NAMESPACE;

/**
 * Implementation of the WebContainer service.
 */
public class PentahoWebPackageBundleListener implements BundleListener {
  private static Logger logger = LoggerFactory.getLogger( PentahoWebPackageBundleListener.class );

  // bundleid -> List services
  final Map<Long, Iterable<ServiceRegistration<IPentahoWebPackage>>> bundleWebPackageServices = new HashMap<>();

  private final JSONParser parser = new JSONParser();

  @Override
  public void bundleChanged( BundleEvent bundleEvent ) {
    final Bundle bundle = bundleEvent.getBundle();
    if ( bundle == null ) {
      return;
    }

    final int bundleEventType = bundleEvent.getType();
    if ( bundleEventType == BundleEvent.STARTED ) {
      addBundle( bundle );
    } else if ( bundleEventType == BundleEvent.UNINSTALLED
        || bundleEventType == BundleEvent.UNRESOLVED
        || bundleEventType == BundleEvent.STOPPED ) {
      removeBundle( bundle );
    }
  }

  public void addBundle( Bundle bundle ) {
    Stream<IPentahoWebPackage> webpackageStream = createWebPackages( bundle );

    // Register services
    List<ServiceRegistration<IPentahoWebPackage>> webpackageServices = webpackageStream.map(
            webpackage -> bundle.getBundleContext().registerService( IPentahoWebPackage.class, webpackage, null )
    ).collect(Collectors.toList());

    if( !webpackageServices.isEmpty() ) {
      synchronized ( this.bundleWebPackageServices ) {
        this.bundleWebPackageServices.putIfAbsent( bundle.getBundleId(), webpackageServices );
      }
    }
  }

  void removeBundle( Bundle bundle ) {
    Iterable<ServiceRegistration<IPentahoWebPackage>> bundleServiceRegistrations = this.bundleWebPackageServices.get( bundle.getBundleId() );

    if ( bundleServiceRegistrations != null ) {
      bundleServiceRegistrations.forEach( this::unregisterService );
      synchronized ( this.bundleWebPackageServices) {
        this.bundleWebPackageServices.remove(bundle.getBundleId());
      }
    }
  }

  private void unregisterService( ServiceRegistration registration ) {
    try {
      registration.unregister();
    } catch ( RuntimeException ignored ) {
      // service might be already unregistered automatically by the bundle lifecycle manager
    }
  }

  List<BundleCapability> getCapabilities( Bundle bundle ) {
    BundleWiring wiring = bundle.adapt( BundleWiring.class );
    if ( wiring != null ) {
      return wiring.getCapabilities( CAPABILITY_NAMESPACE );
    }
    return Collections.emptyList();
  }

  String getRoot( Map<String, Object> attributes ) {
    String root = (String) attributes.getOrDefault("root", "");
    while (root.endsWith("/")) {
      root = root.substring(0, root.length() - 1);
    }
    return root;
  }

  IPentahoWebPackage createWebPackage( Bundle bundle, String capabilityRoot ) {
    try {
      URL packageJsonUrl = bundle.getResource( capabilityRoot + "/package.json" );
      if ( packageJsonUrl != null ) {
        Map<String, Object> packageJson = parsePackageJson( packageJsonUrl );

        String name = (String) packageJson.get( "name" );
        String version = (String) packageJson.get( "version" );

        if ( name != null && version != null ) {
          return new PentahoWebPackageImpl( name, version, ( capabilityRoot.isEmpty() ? "/" : capabilityRoot ), packageJsonUrl );
        }
      } else {
        logger.warn( bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]: " + capabilityRoot + "/package.json not found." );
      }
    } catch ( RuntimeException | ParseException | IOException ignored ) {
      logger.error( bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]: Error parsing " + capabilityRoot + "/package.json." );

      // throwing will make everything fail
      // what damage control should we do?
      // **don't register this capability?** <-- this is what we're doing now
      // ignore and use only the capability info?
      // don't register all the bundle's capabilities?
      // this is all post-bundle wiring phase, so only the requirejs configuration is affected
      // the bundle is started and nothing will change that... or should we bundle.stop()?
    }

    return null;
  }

  Stream<IPentahoWebPackage> createWebPackages( Bundle bundle ) {

    return getCapabilities( bundle ).stream()
            .map(BundleCapability::getAttributes)
            // for now using only the package.json information - so only the `root` attribute is mandatory
            .map( this::getRoot )
            .map( root -> createWebPackage( bundle, root ))
            .filter(Objects::nonNull);
  }

  private Map<String, Object> parsePackageJson( URL resourceUrl ) throws IOException, ParseException {
    URLConnection urlConnection = resourceUrl.openConnection();
    InputStream inputStream = urlConnection.getInputStream();

    InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
    BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

    return (Map<String, Object>) parser.parse( bufferedReader );
  }

}
