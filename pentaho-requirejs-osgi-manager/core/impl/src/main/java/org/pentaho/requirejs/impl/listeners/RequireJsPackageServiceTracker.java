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
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.impl.RequireJsConfigManager;
import org.pentaho.requirejs.impl.RequireJsPackageConfigurationImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks {@link IRequireJsPackage} registered services, providing a corresponding {@link IRequireJsPackageConfiguration} instance.
 */
public class RequireJsPackageServiceTracker implements ServiceTrackerCustomizer<IRequireJsPackage, IRequireJsPackageConfiguration> {
  private BundleContext bundleContext;

  private RequireJsConfigManager requireJsConfigManager;

  private ServiceTracker<IRequireJsPackage, IRequireJsPackageConfiguration> serviceTracker;

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void setRequireJsConfigManager( RequireJsConfigManager requireJsConfigManager ) {
    this.requireJsConfigManager = requireJsConfigManager;
  }

  public void init() {
    this.serviceTracker = new ServiceTracker<>( bundleContext, IRequireJsPackage.class, this );
    this.serviceTracker.open( true );
  }

  public void destroy() {
    this.serviceTracker.close();
    this.serviceTracker = null;
  }

  public List<IRequireJsPackageConfiguration> getPackages() {
    return new ArrayList<>( this.serviceTracker.getTracked().values() );
  }

  @Override
  public IRequireJsPackageConfiguration addingService( ServiceReference<IRequireJsPackage> reference ) {
    Bundle bundle = reference.getBundle();
    // if null then the service is unregistered
    if ( bundle != null ) {
      this.requireJsConfigManager.invalidateCachedConfigurations();

      return new RequireJsPackageConfigurationImpl( this.bundleContext.getService( reference ) );
    }

    return null;
  }

  @Override
  public void modifiedService( ServiceReference<IRequireJsPackage> reference, IRequireJsPackageConfiguration config ) {
    // the RequireJsPackage details might have changed, so it must reprocess it
    config.processRequireJsPackage();

    this.requireJsConfigManager.invalidateCachedConfigurations();
  }

  @Override
  public void removedService( ServiceReference<IRequireJsPackage> reference, IRequireJsPackageConfiguration config ) {
    this.bundleContext.ungetService( reference );

    this.requireJsConfigManager.invalidateCachedConfigurations();
  }
}
