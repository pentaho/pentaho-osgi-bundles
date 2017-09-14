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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
