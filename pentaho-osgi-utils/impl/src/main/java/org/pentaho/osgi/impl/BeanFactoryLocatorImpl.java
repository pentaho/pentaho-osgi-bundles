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

package org.pentaho.osgi.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * User: nbaker Date: 12/17/10
 */
public class BeanFactoryLocatorImpl implements BeanFactoryLocator {

  Logger logger = LoggerFactory.getLogger( getClass() );

  @Override
  public BeanFactory getBeanFactory( Bundle bundle ) {

    try {
      BundleContext bundleContext = bundle.getBundleContext();
      Collection<ServiceReference<BlueprintContainer>> serviceReferences = bundleContext
        .getServiceReferences( BlueprintContainer.class,
          "(osgi.blueprint.container.symbolicname=" + bundle.getSymbolicName() + ")" );
      if ( serviceReferences.size() == 0 ) {
        return null;
      }
      ServiceReference<BlueprintContainer> reference = serviceReferences.iterator().next();
      BlueprintContainer service = bundleContext.getService( reference );
      return new BeanFactoryBlueprintImpl( service );
    } catch ( InvalidSyntaxException e ) {
      logger.error( "Error finding blueprint container", e );
      return null;
    }

  }

  @Override public BeanFactory getBeanFactory( Object serviceObject ) {
    if ( serviceObject instanceof BlueprintContainer ) {
      return new BeanFactoryBlueprintImpl( (BlueprintContainer) serviceObject );
    }
    return null;
  }
}
