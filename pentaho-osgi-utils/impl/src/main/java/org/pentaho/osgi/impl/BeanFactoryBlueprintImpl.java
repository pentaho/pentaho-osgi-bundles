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

import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: nbaker Date: 11/30/10
 */
public class BeanFactoryBlueprintImpl implements BeanFactory {
  private BlueprintContainer blueprintContainer;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public BeanFactoryBlueprintImpl( BlueprintContainer blueprintContainer ) {
    this.blueprintContainer = blueprintContainer;
  }

  @Override
  public Object getInstance( String id ) {
    return blueprintContainer.getComponentInstance( id );
  }


  @Override
  public <T> T getInstance( String id, Class<T> classType ) {
    Object beanFromContainer = blueprintContainer.getComponentInstance( id );
    return (T) beanFromContainer;
  }
}
