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

package org.pentaho.requirejs;

import java.net.URL;
import java.util.List;

/**
 * Collection of RequireJS configuration files provided by a Platform Plugin
 * (external resources files declared in plugin.xml with context="requirejs").
 */
public interface IPlatformPluginRequireJsConfigurations {
  /**
   * Returns the list of RequireJS configuration files provided by this Platform Plugin.
   *
   * @return List of URLs of the configuration files.
   */
  List<URL> getRequireConfigurationsURLs();

  /**
   * Provides a not necessarily unique human-readable identifier for this collection of RequireJS configuration files.
   *
   * @return The name of this resource collection.
   */
  String getName();
}
