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
