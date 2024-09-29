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
