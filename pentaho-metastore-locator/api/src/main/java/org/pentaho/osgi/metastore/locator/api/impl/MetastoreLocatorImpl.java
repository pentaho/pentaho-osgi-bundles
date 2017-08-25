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
package org.pentaho.osgi.metastore.locator.api.impl;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.osgi.blueprint.collection.utils.ServiceMap;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;

import java.util.UUID;

/**
 * Created by tkafalas on 6/19/2017
 */
public class MetastoreLocatorImpl extends ServiceMap<MetastoreProvider> implements MetastoreLocator,
  MetastoreLocatorOsgi {

  // Returns the exact metastore defined by the key
  @Override
  public IMetaStore getExplicitMetastore( String providerKey ) {
    MetastoreProvider provider = super.getItem( providerKey );
    return provider == null ? null : provider.getMetastore();
  }

  @Override
  public IMetaStore getMetastore() {
    return getMetastore( null );
  }

  @Override
  public IMetaStore getMetastore( String providerKey ) {
    IMetaStore metaStore = getExplicitMetastore( MetastoreLocator.REPOSITORY_PROVIDER_KEY );
    if ( metaStore == null ) {
      metaStore = getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY );
    }
    if ( metaStore == null && providerKey != null ) {
      metaStore = getExplicitMetastore( providerKey );
    }
    if ( metaStore == null ) {
      try {
        metaStore = MetaStoreConst.openLocalPentahoMetaStore( false );
      } catch ( MetaStoreException e ) {
        return null;
      }
    }

    return metaStore;
  }

  @Override
  public String setEmbeddedMetastore( final IMetaStore metastore ) {
    MetastoreProvider metastoreProvider = new MetastoreProvider() {

      @Override public IMetaStore getMetastore() {
        return metastore;
      }
    };
    UUID uuid = UUID.randomUUID();
    String providerKey = EMBEDDED_METASTORE_KEY_PREFIX + uuid.toString();
    itemAdded( metastoreProvider, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, providerKey ) );
    return providerKey;
  }

  @Override
  public void disposeMetastoreProvider( String providerKey ) {
    itemRemoved( null, ImmutableMap.of( ServiceMap.SERVICE_KEY_PROPERTY, providerKey ) );
  }

}
