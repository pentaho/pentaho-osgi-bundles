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
 * Implementations of this class provide access to various system Capabilities. There's no explicit SPI, but
 * implementations of ICapabilityManager need to have some way to find these providers, or they must be registered
 * with the Manager in some way.
 * <p>
 * Created by nbaker on 4/6/15.
 */
public interface ICapabilityProvider {

  /**
   * Unique ID for this Provider
   *
   * @return
   */
  String getId();

  /**
   * Get a list of Capability IDs found by the provider
   *
   * @return
   */
  Set<String> listCapabilities();

  /**
   * Get a Capability by ID
   *
   * @param id
   * @return
   */
  ICapability getCapabilityById( String id );


  /**
   * Returns true if capability exists, if not return false
   *
   * @param id
   * @return
   */
  boolean capabilityExist( String id );

  /**
   * Get a set containing all ICapabilities
   *
   * @return
   */
  Set<ICapability> getAllCapabilities();
}
