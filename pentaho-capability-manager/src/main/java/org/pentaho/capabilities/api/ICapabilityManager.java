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
package org.pentaho.capabilities.api;

import java.util.Set;

/**
 *
 * An extension of the ICapabilityProvider interface which adds methods supporting multiple providers. Implementations
 * of this interface should aggregate together the results of calling each registered provider.
 *
 * Created by nbaker on 4/6/15.
 */
public interface ICapabilityManager extends ICapabilityProvider {

  /**
   * get a Set containing the id of all registered ICapabilityProviders.
   * @return Set containing the IDs of all registered providers
   */
  Set<String> listProviders();

  /**
   * Retrieve an ICapabilityProvider by ID
   *
   * @param id
   * @return provider registered by the given ID or null
   */
  ICapabilityProvider getProvider( String id );
}
