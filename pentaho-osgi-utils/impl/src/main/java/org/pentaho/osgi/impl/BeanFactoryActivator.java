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
package org.pentaho.osgi.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.pentaho.osgi.api.BlueprintStateService;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.osgi.api.ProxyUnwrapper;

/**
 * User: nbaker Date: 12/17/10
 */
public class BeanFactoryActivator implements BundleActivator {

  @Override
  public void start( BundleContext bundleContext ) throws Exception {
    BlueprintStateServiceImpl blueprintServiceImpl = new BlueprintStateServiceImpl( bundleContext );
    bundleContext
        .registerService( new String[] { BlueprintListener.class.getName(), BlueprintStateService.class.getName() },
            blueprintServiceImpl, null );

    KarafBlueprintWatcherImpl karafBlueprintWatcher = new KarafBlueprintWatcherImpl( bundleContext );
    bundleContext.registerService( IKarafBlueprintWatcher.class.getName(), karafBlueprintWatcher, null );

    bundleContext.registerService( ProxyUnwrapper.class.getName(), new ProxyUnwrapperImpl( bundleContext ), null );
    bundleContext.registerService( BeanFactoryLocator.class.getName(), new BeanFactoryLocatorImpl(), null );
    ServiceReference ref = bundleContext.getServiceReference( ConfigurationAdmin.class.getName() );
    ConfigurationAdmin admin = (ConfigurationAdmin) bundleContext.getService( ref );

    KarafFeatureWatcherImpl karafFeatureWatcher = new KarafFeatureWatcherImpl( bundleContext );
    bundleContext.registerService( IKarafFeatureWatcher.class.getName(), karafFeatureWatcher, null );
  }

  @Override
  public void stop( BundleContext bundleContext ) throws Exception {

  }
}
