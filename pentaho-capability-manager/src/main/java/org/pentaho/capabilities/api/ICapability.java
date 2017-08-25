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
