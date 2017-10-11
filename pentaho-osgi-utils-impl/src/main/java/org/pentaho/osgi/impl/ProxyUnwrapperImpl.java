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
 * Copyright 2015 - 2017 Hitachi Vantara. All rights reserved.
 */

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
