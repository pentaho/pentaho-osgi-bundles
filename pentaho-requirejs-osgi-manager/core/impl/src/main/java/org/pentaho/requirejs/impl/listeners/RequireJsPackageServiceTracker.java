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
package org.pentaho.requirejs.impl.listeners;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.pentaho.requirejs.RequireJsPackage;
import org.pentaho.requirejs.RequireJsPackageConfiguration;
import org.pentaho.requirejs.impl.RequireJsConfigManager;
import org.pentaho.requirejs.impl.RequireJsPackageConfigurationImpl;

/**
 * Tracks {@link RequireJsPackage} registered services, providing a corresponding {@link RequireJsPackageConfiguration} instance.
 */
public class RequireJsPackageServiceTracker implements ServiceTrackerCustomizer<RequireJsPackage, RequireJsPackageConfiguration> {
  private final BundleContext context;
  private final RequireJsConfigManager requireJsConfigManager;

  public RequireJsPackageServiceTracker( BundleContext context, RequireJsConfigManager requireJsConfigManager ) {
    this.context = context;
    this.requireJsConfigManager = requireJsConfigManager;
  }

  @Override
  public RequireJsPackageConfiguration addingService( ServiceReference<RequireJsPackage> reference ) {
    Bundle bundle = reference.getBundle();
    // if null then the service is unregistered
    if ( bundle != null ) {
      this.requireJsConfigManager.invalidateCachedConfigurations();

      return new RequireJsPackageConfigurationImpl( this.context.getService( reference ) );
    }

    return null;
  }

  @Override
  public void modifiedService( ServiceReference<RequireJsPackage> reference, RequireJsPackageConfiguration config ) {
    // the RequireJsPackage details might have changed, so it must reprocess it
    config.processRequireJsPackage();

    this.requireJsConfigManager.invalidateCachedConfigurations();
  }

  @Override
  public void removedService( ServiceReference<RequireJsPackage> reference, RequireJsPackageConfiguration config ) {
    this.context.ungetService( reference );

    this.requireJsConfigManager.invalidateCachedConfigurations();
  }
}
