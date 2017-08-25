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

package org.pentaho.platform.proxy.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbaker on 8/10/15.
 */
public class ProxyRequestTracker {
  private Map<Class<?>, ProxyTargetServiceTracker> classesToTrack = new HashMap<>();
  private BundleContext bundleContext;
  private IProxyFactory proxyFactory;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public ProxyRequestTracker( BundleContext bundleContext, IProxyFactory proxyFactory ) {
    this.bundleContext = bundleContext;
    this.proxyFactory = proxyFactory;
  }

  public void registrationRemoved( ProxyRequestRegistration registration ) {
    if ( registration == null ) {
      // Strange but does happen
      return;
    }
    ProxyTargetServiceTracker tracker = classesToTrack.remove( registration.getClassForProxying() );
    tracker.close();
  }

  public void registrationAdded( ProxyRequestRegistration registration ) {
    Class<?> classForProxying = registration.getClassForProxying();
    ProxyTargetServiceTracker serviceTracker = new ProxyTargetServiceTracker( classForProxying );
    serviceTracker.open();
    classesToTrack.put( classForProxying, serviceTracker );
  }

  Map<Class<?>, ProxyTargetServiceTracker> getClassesToTrack() {
    return Collections.unmodifiableMap( classesToTrack );
  }

  private static Map<String, Object> extractPropertiesFromReference( ServiceReference<?> reference ) {
    Map<String, Object> properties = new HashMap<String, Object>();
    for ( String s : reference.getPropertyKeys() ) {
      properties.put( s, reference.getProperty( s ) );
    }
    return properties;
  }


  class ProxyTargetServiceTracker<S, T> extends ServiceTracker<S, T> {
    private Class<S> clazz;

    public ProxyTargetServiceTracker( final Class<S> clazz ) {
      super( bundleContext, clazz, null );
      this.clazz = clazz;
    }

    private final HashMap<ServiceReference, IProxyRegistration> registrations = new HashMap<>();

    @SuppressWarnings( "unchecked" )
    @Override public T addingService( ServiceReference<S> serviceReference ) {
      IProxyRegistration proxyRegistration = null;
      S service = bundleContext.getService( serviceReference );
      try {
        proxyRegistration = proxyFactory
            .createAndRegisterProxy( service, Collections.<Class<?>>singletonList( clazz ),
                extractPropertiesFromReference( serviceReference ) );
        registrations.put( serviceReference, proxyRegistration );
      } catch ( ProxyException e ) {
        logger.error( "Error Proxying Object: " + service.getClass(), e );
      }

      return (T) service;
    }

    @Override public void modifiedService( ServiceReference<S> serviceReference, T t ) {
      removedService( serviceReference, t );
      addingService( serviceReference );
    }

    @Override public void removedService( ServiceReference<S> serviceReference, T t ) {
      registrations.remove( serviceReference ).getPentahoObjectRegistration().remove();
    }
  }

}
