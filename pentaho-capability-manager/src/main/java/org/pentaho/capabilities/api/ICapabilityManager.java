/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
