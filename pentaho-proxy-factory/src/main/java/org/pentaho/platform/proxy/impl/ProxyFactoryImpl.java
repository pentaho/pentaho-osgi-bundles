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
 */package org.pentaho.platform.proxy.impl;

import org.pentaho.osgi.api.ProxyUnwrapper;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nbaker on 8/10/15.
 */
public class ProxyFactoryImpl implements IProxyFactory {
  private List<IProxyCreator<?>> creators = new ArrayList<>();
  private Logger logger = LoggerFactory.getLogger( getClass() );
  private ProxyUnwrapper proxyUnwrapper;

  public ProxyFactoryImpl( ProxyUnwrapper proxyUnwrapper ) {
    this.proxyUnwrapper = proxyUnwrapper;
  }

  @Override public <T, K> K createProxy( T target ) throws ProxyException {
    Class<T> targetClass = (Class<T>) target.getClass();

    if ( proxyUnwrapper != null ) {
      // make sure we're dealing with the real target, not a proxy from OSGI
      target = (T) proxyUnwrapper.unwrap( target );
      targetClass = (Class<T>) target.getClass();
    }

    logger.debug( "Proxy Request initiated for class: " + targetClass );

    // Loops through in succession of the class hierarchy to ensure that the most specific ProxyCreator is used.

    logger.debug( "Attempting to find Proxy Creator by class hierarchy: " + targetClass );
    Class<?> parentClass = targetClass;
    K proxyWrapper = null;

    outer:
    while ( parentClass != null ) {
      for ( IProxyCreator<?> creator : creators ) {
        if ( creator.supports( parentClass ) ) {
          logger.debug( "Proxy creator found for : " + targetClass + " : " + creator.getClass() );
          proxyWrapper = (K) creator.create( target );
          if ( proxyWrapper != null ) {
            break outer;
          }
        }
      }
      parentClass = parentClass.getSuperclass();
    }

    if ( proxyWrapper == null ) {
      // Not found with Class heriarchy. Lets try the interface chain.
      logger.debug( "Attempting to find Proxy Creator by declared Interfaces: " + targetClass );

      parentClass = targetClass;
      outer:
      while ( parentClass != null ) {
        Class<?>[] interfaces = parentClass.getInterfaces();
        for ( Class<?> anInterface : interfaces ) {
          for ( IProxyCreator<?> creator : creators ) {
            if ( creator.supports( anInterface ) ) {
              logger.debug( "Proxy creator found for : " + targetClass + " : " + creator.getClass() );
              proxyWrapper = (K) creator.create( target );
              if ( proxyWrapper != null ) {
                break outer;
              }
            }
          }
        }
        parentClass = parentClass.getSuperclass();
      }

    }
    if ( proxyWrapper == null ) {
      // No creator configured for the supplied target.
      throw new ProxyException(
          "No Proxy Creator found for supplied target class: " + targetClass.getName() );
    }
    return proxyWrapper;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T, K> IProxyRegistration createAndRegisterProxy( T target, List<Class<?>> publishedClasses,
                                                           Map<String, Object> properties )
      throws ProxyException {

    publishedClasses = new ArrayList<>( publishedClasses );
    K proxyWrapper = createProxy( target );

    Class<K> proxyWrapperClass = (Class<K>) proxyWrapper.getClass();
    Class parent = proxyWrapperClass;
    while ( parent != null ) {
      publishedClasses.add( parent );
      parent = parent.getSuperclass();
    }

    for ( Class<?> aClass : proxyWrapperClass.getInterfaces() ) {
      publishedClasses.add( aClass );
    }

    IPentahoObjectReference reference =
        new SingletonPentahoObjectReference.Builder<K>( proxyWrapperClass ).object( proxyWrapper )
            .attributes( properties ).build();

    IPentahoObjectRegistration iPentahoObjectRegistration = PentahoSystem
        .registerReference( reference, publishedClasses.toArray( new Class<?>[ publishedClasses.size() ] ) );
    return new ProxyRegistration( iPentahoObjectRegistration, proxyWrapper );

  }

  public void setCreators( List<IProxyCreator<?>> creators ) {
    // assign the incoming list as it may be a resizing proxy from OSGI
    this.creators = creators;
  }
}
