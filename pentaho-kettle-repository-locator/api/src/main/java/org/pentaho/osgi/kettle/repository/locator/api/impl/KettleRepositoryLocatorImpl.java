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
