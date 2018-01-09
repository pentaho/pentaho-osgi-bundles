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
