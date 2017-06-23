/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

package org.pentaho.osgi.metastore.locator.api.impl;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.osgi.blueprint.collection.utils.RankedList;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;
import org.pentaho.osgi.metastore.locator.api.MetastoreProvider;

import java.util.Objects;

/**
 * Created by bryan on 3/28/16.
 */
public class MetastoreLocatorImpl extends RankedList<MetastoreProvider> implements MetastoreLocator {
  public MetastoreLocatorImpl() {
    super( ( o1, o2 ) -> o1.toString().compareTo( o2.toString() ) );
  }

  @Override public IMetaStore getMetastore() {
    return getList().stream().map( MetastoreProvider::getMetastore ).filter( Objects::nonNull ).findFirst()
      .orElse( null );
  }
}
