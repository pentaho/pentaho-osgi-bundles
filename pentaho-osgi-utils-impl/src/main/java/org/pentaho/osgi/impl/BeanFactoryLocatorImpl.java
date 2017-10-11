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
 * Copyright 2015-2017 Hitachi Vantara. All rights reserved.
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
  private static final String CONTAINER_KEY = "org.osgi.service.blueprint.container.BlueprintContainer";
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
