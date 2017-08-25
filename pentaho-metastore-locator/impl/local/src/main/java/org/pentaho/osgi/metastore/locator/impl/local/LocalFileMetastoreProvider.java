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

package org.pentaho.osgi.metastore.locator.impl.local;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bryan on 3/28/16.
 */
public class LocalFileMetastoreProvider implements MetastoreProvider {
  private static final Logger logger = LoggerFactory.getLogger( LocalFileMetastoreProvider.class );
  private final MetastoreSupplier supplier;

  public LocalFileMetastoreProvider() {
    this( MetaStoreConst::openLocalPentahoMetaStore );
  }

  public LocalFileMetastoreProvider( MetastoreSupplier supplier ) {
    this.supplier = supplier;
  }

  @Override public IMetaStore getMetastore() {
    try {
      return supplier.getMetastore();
    } catch ( MetaStoreException e ) {
      logger.error( "Unable to open local metastore", e );
      return null;
    }
  }

  @VisibleForTesting interface MetastoreSupplier {
    IMetaStore getMetastore() throws MetaStoreException;
  }
}
