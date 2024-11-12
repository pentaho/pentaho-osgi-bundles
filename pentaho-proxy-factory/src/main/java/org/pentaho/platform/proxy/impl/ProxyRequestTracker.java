/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
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
