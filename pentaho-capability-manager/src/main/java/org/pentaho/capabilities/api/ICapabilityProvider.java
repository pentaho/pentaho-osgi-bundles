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
