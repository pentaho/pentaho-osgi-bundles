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
package org.pentaho.osgi.metastore.locator.api;

import org.pentaho.metastore.api.IMetaStore;

/**
 * Created by tkafalas on 6/19/2017
 */
public interface MetastoreLocator {
  final String LOCAL_PROVIDER_KEY = "LocalMetastoreProvider";
  final String REPOSITORY_PROVIDER_KEY = "RepositoryMetastoreProvider";

  /**
   * Attempts to pick the best the MetaStore based on environment; either
   * the local or repository metastore.
   * @return The metastore to use
   */
  IMetaStore getMetastore();

  /**
   * Works similar to (@link #getMetastore()) except that it will fall back to a metastore with a key of
   * providerKey if the both repository and local metastore both are not found.
   * @param providerKey
   * @return
   */
  IMetaStore getMetastore( String providerKey );

  /**
   * Registers a metastore provider that returns the received metastore with the current thread. {@link #getMetastore()}
   * will used this metastore iff the repository and local metastore provider cannot be found.
   * @param metastore
   * @return
   */
  String setEmbeddedMetastore( IMetaStore metastore );

  /**
   * Dispose a metastore provider assciated with the providerKey
   * @param providerKey The key to the metastore provider.
   */
  public void disposeMetastoreProvider( String providerKey );

  /**
   * Unconditionally returns the metastore stored with the given provider key, or null if it does not exist.
   * @param providerKey
   * @return
   */
  public IMetaStore getExplicitMetastore( String providerKey );
}
