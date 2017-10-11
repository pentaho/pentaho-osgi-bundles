/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

package org.pentaho.osgi.kettle.repository.locator.api.impl;

import org.pentaho.di.repository.Repository;
import org.pentaho.osgi.blueprint.collection.utils.RankedList;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryLocator;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryProvider;

import java.util.Objects;

/**
 * Created by bryan on 4/15/16.
 */
public class KettleRepositoryLocatorImpl extends RankedList<KettleRepositoryProvider>
  implements KettleRepositoryLocator {
  public KettleRepositoryLocatorImpl() {
    super( ( o1, o2 ) -> o1.toString().compareTo( o2.toString() ) );
  }

  @Override public Repository getRepository() {
    return getList().stream().map( KettleRepositoryProvider::getRepository ).filter( Objects::nonNull ).findFirst()
      .orElse( null );
  }
}
