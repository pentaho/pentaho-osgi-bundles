/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
