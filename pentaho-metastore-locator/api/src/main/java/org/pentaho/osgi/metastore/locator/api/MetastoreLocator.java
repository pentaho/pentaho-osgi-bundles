/*!
 * Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.osgi.metastore.locator.api;

import org.pentaho.metastore.api.IMetaStore;

/**
 * Created by tkafalas on 6/19/2017
 */
public interface MetastoreLocator {
  final String LOCAL_PROVIDER_KEY = "LocalMetastoreProvider";
  final String CARTE_PROVIDER_KEY = "CarteMetastoreProvider";
  final String PMR_PROVIDER_KEY = "PmrMetastoreProvider";
  final String REPOSITORY_PROVIDER_KEY = "RepositoryMetastoreProvider";

  IMetaStore getMetastore( String providerKey );

  /**
   * Attempts to pick the best the MetaStore based on environment; either
   * the local or repository metastore.
   * @return
   */
  IMetaStore getMetastore();

}
