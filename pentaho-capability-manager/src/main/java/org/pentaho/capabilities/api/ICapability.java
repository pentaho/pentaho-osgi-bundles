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
