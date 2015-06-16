package org.pentaho.capabilities.api;

import java.util.Set;

/**
 * Implementations of this class provide access to various system Capabilities. There's no explicit SPI, but
 * implementations of ICapabilityManager need to have some way to find these providers, or they must be registered
 * with the Manager in some way.
 *
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
   * Get a set containing all ICapabilities
   *
   * @return
   */
  Set<ICapability> getAllCapabilities();
}
