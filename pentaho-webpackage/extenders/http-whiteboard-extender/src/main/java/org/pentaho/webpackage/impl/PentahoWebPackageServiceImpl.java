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
 * Copyright 2017 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.webpackage.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.pentaho.webpackage.core.PentahoWebPackageBundle;
import org.pentaho.webpackage.core.PentahoWebPackageService;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the WebContainer service.
 */
public class PentahoWebPackageServiceImpl implements PentahoWebPackageService, BundleListener {
  private final Map<Long, PentahoWebPackageBundle> pentahoWebPackageBundles = new HashMap<>();

  @Override
  public void bundleChanged( BundleEvent bundleEvent ) {
    final Bundle bundle = bundleEvent.getBundle();

    final int bundleEventType = bundleEvent.getType();
    if ( bundleEventType == BundleEvent.STARTED ) {
      addBundle( bundle );
    } else if ( bundleEventType == BundleEvent.UNINSTALLED
        || bundleEventType == BundleEvent.UNRESOLVED
        || bundleEventType == BundleEvent.STOPPED ) {
      removeBundle( bundle );
    }
  }

  @Override
  public void addBundle( Bundle bundle ) {
    PentahoWebPackageBundle extendedBundle = extendBundle( bundle );

    if ( extendedBundle != null ) {
      synchronized ( this.pentahoWebPackageBundles ) {
        if ( this.pentahoWebPackageBundles.putIfAbsent( bundle.getBundleId(), extendedBundle ) != null ) {
          return;
        }
      }

      extendedBundle.init();
    }
  }

  @Override
  public void removeBundle( Bundle bundle ) {
    if ( bundle == null ) {
      return;
    }

    PentahoWebPackageBundle pwpc = this.pentahoWebPackageBundles.remove( bundle.getBundleId() );
    if ( pwpc != null ) {
      pwpc.destroy();
    }
  }

  private PentahoWebPackageBundleImpl extendBundle( final Bundle bundle ) {
    if ( bundle == null ) {
      return null;
    }

    // Check that this is a web bundle
    String provideCapabilityHeader = getHeader( bundle, "Provide-Capability" );
    if ( provideCapabilityHeader == null || !provideCapabilityHeader.contains( CAPABILITY_NAMESPACE ) ) {
      return null;
    }

    return new PentahoWebPackageBundleImpl( bundle );
  }

  private String getHeader( final Bundle bundle, String... keys ) {
    // Look in the bundle...
    Dictionary<String, String> headers = bundle.getHeaders();
    if ( headers != null ) {
      for ( String key : keys ) {
        String value = headers.get( key );
        if ( value != null ) {
          return value;
        }
      }
    }

    // Fragment bundles are unsupported for now
//    BundleContext bundleContext = bundle.getBundleContext();
//
//    Bundle[] bundles = bundleContext.getBundles();
//    if ( bundles != null ) {
//      for ( Bundle fragment : bundles ) {
//        // fragments are always only in resolved state
//        if ( fragment.getState() != Bundle.RESOLVED ) {
//          continue;
//        }
//
//        // A fragment must also have the FRAGMENT_HOST header and the
//        // FRAGMENT_HOST header must be equal to the bundle symbolic name
//        String fragmentHost = fragment.getHeaders().get( Constants.FRAGMENT_HOST );
//        if ( ( fragmentHost == null ) || ( !fragmentHost.equals( bundle.getSymbolicName() ) ) ) {
//          continue;
//        }
//
//        headers = fragment.getHeaders();
//        for ( String key : keys ) {
//          String value = headers.get( key );
//          if ( value != null ) {
//            return value;
//          }
//        }
//      }
//    }

    return null;
  }
}
