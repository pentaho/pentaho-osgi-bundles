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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * User: nbaker Date: 11/30/10
 */
public class BeanFactoryBlueprintImpl implements BeanFactory {
  private BlueprintContainer blueprintContainer;
  private BundleContext bundleContext;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public BeanFactoryBlueprintImpl( BlueprintContainer blueprintContainer, BundleContext bundleContext ) {
    this.blueprintContainer = blueprintContainer;
    this.bundleContext = bundleContext;
  }

  @Override
  public Object getInstance( String id ) {
    return blueprintContainer.getComponentInstance(id);
  }


  @Override
  public <T> T getInstance( String id, Class<T> classType ) {
    Object beanFromContainer = blueprintContainer.getComponentInstance( id );
    return (T) beanFromContainer;
  }
}
