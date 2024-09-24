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
