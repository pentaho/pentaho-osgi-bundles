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
