/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.capabilities.api;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * Represents a capability of the system. There's no limit to what a capability can be. It may represent a single
 * plugin, a collection of plugins, OSGI bundles, Jars, Samples, anything which can be managed and installed into the
 * system atomically.
 *
 * Capabilities are supplied by ICapabilityProvider instances.
 *
 * TODO: There's potential for name conflicts with capabilities from different providers having the same ID. We may need
 * to switch to a composite key of provider id + capability id.
 *
 * Created by nbaker on 4/6/15.
 */
public interface ICapability {

  /**
   * ID Representing this capability. This must be unique by provider
   * @return
   */
  String getId();

  /**
   * Provides a description of the capability.
   *
   * @param locale
   * @return
   */
  String getDescription( Locale locale );

  /**
   * Checks whether or not the capability is installed.
   * @return
   */
  boolean isInstalled();

  /**
   * Installs the capability if it's not already.
   * @return a Future to check if the installation was successful.
   */
  Future<Boolean> install();

  /**
   * Uninstall the capability if it's installed
   * @return a Future to check if the uninstallation was successful.
   */
  Future<Boolean> uninstall();

  /**
   * Get an external representation of the capability.
   *
   * This is currently not used and may be removed.
   * @return
   */
  URI getSourceUri();
}
