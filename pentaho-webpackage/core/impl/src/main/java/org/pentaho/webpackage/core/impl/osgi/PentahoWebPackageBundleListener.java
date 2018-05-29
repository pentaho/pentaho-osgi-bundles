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
package org.pentaho.webpackage.core.impl.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.IPentahoWebPackage;
import org.pentaho.webpackage.core.impl.PentahoWebPackageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pentaho.webpackage.core.PentahoWebPackageConstants.CAPABILITY_NAMESPACE;

/**
 * Implementation of the WebContainer service.
 */
public class PentahoWebPackageBundleListener implements BundleListener {
  private static Logger logger = LoggerFactory.getLogger( PentahoWebPackageBundleListener.class );

  // BundleId -> WebPackage Service References
  final Map<Long, Iterable<ServiceRegistration<IPentahoWebPackage>>> bundleWebPackageServiceRegistrations = new HashMap<>();

  @Override
  public void bundleChanged( BundleEvent bundleEvent ) {
    final Bundle bundle = bundleEvent.getBundle();

    final int bundleEventType = bundleEvent.getType();
    if ( bundleEventType == BundleEvent.STARTED ) {
      registerWebPackageServices( bundle );
    } else if ( bundleEventType == BundleEvent.UNINSTALLED
        || bundleEventType == BundleEvent.UNRESOLVED
        || bundleEventType == BundleEvent.STOPPED ) {
      unregisterWebPackageServices( bundle );
    }
  }

  public void registerWebPackageServices( Bundle bundle ) {
    if ( bundle == null ) {
      return;
    }

    // Create WebPackages
    Stream<IPentahoWebPackage> webPackages = createWebPackages( bundle );

    // Register WebPackages Services
    BundleContext bundleContext = bundle.getBundleContext();
    List<ServiceRegistration<IPentahoWebPackage>> webpackageServiceRegistrations = webPackages
            .map( webpackage -> bundleContext.registerService( IPentahoWebPackage.class, webpackage, null ) )
            .collect( Collectors.toList() );

    if ( !webpackageServiceRegistrations.isEmpty() ) {
      synchronized ( this.bundleWebPackageServiceRegistrations ) {
        this.bundleWebPackageServiceRegistrations.putIfAbsent( bundle.getBundleId(), webpackageServiceRegistrations );
      }
    }
  }

  public void unregisterWebPackageServices( Bundle bundle ) {
    if ( bundle == null ) {
      return;
    }

    Iterable<ServiceRegistration<IPentahoWebPackage>> bundleServiceRegistrations = this.getBundleServiceRegistrations( bundle.getBundleId() );

    if ( bundleServiceRegistrations != null ) {
      bundleServiceRegistrations.forEach( this::unregisterService );
      synchronized ( this.bundleWebPackageServiceRegistrations ) {
        this.bundleWebPackageServiceRegistrations.remove( bundle.getBundleId() );
      }
    }
  }

  Iterable<ServiceRegistration<IPentahoWebPackage>> getBundleServiceRegistrations( long bundleId ) {
    return this.bundleWebPackageServiceRegistrations.get( bundleId );
  }

  private void unregisterService( ServiceRegistration registration ) {
    try {
      registration.unregister();
    } catch ( RuntimeException ignored ) {
      // service might be already unregistered automatically by the bundle lifecycle manager
    }
  }

  List<BundleCapability> getWebPackageCapabilities( Bundle bundle ) {
    BundleWiring wiring = bundle.adapt( BundleWiring.class );
    if ( wiring != null ) {
      return wiring.getCapabilities( CAPABILITY_NAMESPACE );
    }
    return Collections.emptyList();
  }

  IPentahoWebPackage createWebPackage( Bundle bundle, BundleCapability webPackageCapability ) {
    String capabilityRoot = getRoot( webPackageCapability );
    try {
      URL packageJsonUrl = bundle.getResource( capabilityRoot + "package.json" );
      if ( packageJsonUrl != null ) {
        return new PentahoWebPackageImpl( capabilityRoot, packageJsonUrl );
      } else {
        logger.warn( bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]: " + capabilityRoot + "package.json not found." );
      }
    } catch ( RuntimeException ignored ) {
      logger.error( bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]: Error parsing " + capabilityRoot + "package.json." );

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
    return getWebPackageCapabilities( bundle ).stream()
        .map( capability -> createWebPackage( bundle, capability ) )
        .filter( Objects::nonNull );
  }

  String getRoot( BundleCapability capability ) {
    String root = (String) capability.getAttributes().getOrDefault( "root", "/" );

    if ( !root.endsWith( "/" ) ) {
      // ensure it ends with slash
      root = root + "/";
    } else {
      // trim extra slashes
      while ( root.endsWith( "//" ) ) {
        root = root.substring( 0, root.length() - 1 );
      }
    }

    return root;
  }
}
