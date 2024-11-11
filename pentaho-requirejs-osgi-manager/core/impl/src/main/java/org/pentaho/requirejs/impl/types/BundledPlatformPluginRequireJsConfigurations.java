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
