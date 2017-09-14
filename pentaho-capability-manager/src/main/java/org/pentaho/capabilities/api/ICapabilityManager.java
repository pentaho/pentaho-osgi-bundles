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
 *
 * An extension of the ICapabilityProvider interface which adds methods supporting multiple providers. Implementations
 * of this interface should aggregate together the results of calling each registered provider.
 *
 * Created by nbaker on 4/6/15.
 */
public interface ICapabilityManager extends ICapabilityProvider{

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
