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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.pentaho.webpackage.core.PentahoWebPackageBundle;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the WebContainer service.
 */
public class PentahoWebPackageBundleListener implements BundleListener {
  private final Map<Long, PentahoWebPackageBundleImpl> pentahoWebPackageBundles = new HashMap<>();

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

  public void addBundle( Bundle bundle ) {
    PentahoWebPackageBundleImpl extendedBundle = extendBundle( bundle );

    if ( extendedBundle != null ) {
      synchronized ( this.pentahoWebPackageBundles ) {
        if ( this.pentahoWebPackageBundles.putIfAbsent( bundle.getBundleId(), extendedBundle ) != null ) {
          return;
        }
      }

      extendedBundle.init();
    }
  }

  public void removeBundle( Bundle bundle ) {
    if ( bundle == null ) {
      return;
    }

    PentahoWebPackageBundleImpl pwpc = this.pentahoWebPackageBundles.remove( bundle.getBundleId() );
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
    if ( provideCapabilityHeader == null || !provideCapabilityHeader.contains( PentahoWebPackageBundle.CAPABILITY_NAMESPACE ) ) {
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
