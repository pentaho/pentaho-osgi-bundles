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

package org.pentaho.osgi.impl;

import org.apache.aries.proxy.ProxyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.osgi.api.ProxyUnwrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * User: RFellows Date: 2/19/15
 */
public class ProxyUnwrapperImpl implements ProxyUnwrapper {
  private BundleContext bundleContext;
  private ProxyManager proxyManager;
  private static final Logger logger = LoggerFactory.getLogger( ProxyUnwrapperImpl.class );

  public ProxyUnwrapperImpl( BundleContext bundleContext ) {
    setBundleContext( bundleContext );
    init();
  }

  private void init() {
    if ( proxyManager == null ) {
      ServiceReference<ProxyManager> serviceReference = bundleContext.getServiceReference( ProxyManager.class );
      proxyManager = bundleContext.getService( serviceReference );
    }
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void setProxyManager( ProxyManager proxyManager ) {
    this.proxyManager = proxyManager;
  }

  @Override
  public Object unwrap( Object proxyWrappedObject ) {
    if ( proxyManager.isProxy( proxyWrappedObject ) ) {
      Callable<Object> callable = proxyManager.unwrap( proxyWrappedObject );
      try {
        proxyWrappedObject = callable.call();
      } catch ( Exception e ) {
        // something went wrong
        logger.error( "Could not unwrap proxied object", e );
      }
    }
    return proxyWrappedObject;
  }
}
