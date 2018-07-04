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
package org.pentaho.requirejs.impl.types;

import org.osgi.framework.Bundle;
import org.pentaho.requirejs.IPlatformPluginRequireJsConfigurations;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collection of RequireJS configuration files provided by a Platform Plugin processed and deployed by the pentaho-platform-plugin-deployer.
 */
public class BundledPlatformPluginRequireJsConfigurations implements IPlatformPluginRequireJsConfigurations {
  private final Bundle bundle;

  private final List<String> requireConfigurations;

  public BundledPlatformPluginRequireJsConfigurations( Bundle bundle, List<String> requireConfigurations ) {
    this.bundle = bundle;

    this.requireConfigurations = requireConfigurations;
  }

  @Override
  public List<URL> getRequireConfigurationsURLs() {
    return requireConfigurations.stream().map( bundle::getResource ).collect( Collectors.toList() );
  }

  @Override
  public String getName() {
    return "[" + bundle.getBundleId() + "] - " + bundle.getSymbolicName() + ":" + bundle.getVersion();
  }
}
