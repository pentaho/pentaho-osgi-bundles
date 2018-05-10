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
package org.pentaho.osgi.metastore.locator.impl.repository;

import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryLocator;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;

/**
 * Created by bryan on 3/29/16.
 */
public class RepositoryMetastoreProvider implements MetastoreProvider {
  private final KettleRepositoryLocator kettleRepositoryLocator;

  public RepositoryMetastoreProvider( KettleRepositoryLocator kettleRepositoryLocator ) {
    this.kettleRepositoryLocator = kettleRepositoryLocator;
  }

  @Override public IMetaStore getMetastore() {
    Repository repository = kettleRepositoryLocator.getRepository();
    if ( repository != null ) {
      return repository.getMetaStore();
    }
    return null;
  }
}
