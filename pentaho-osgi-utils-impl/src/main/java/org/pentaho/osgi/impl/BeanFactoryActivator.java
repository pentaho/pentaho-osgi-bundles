package org.pentaho.osgi.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.pentaho.osgi.api.IKarafFeatureWatcher;

/**
 * User: nbaker Date: 12/17/10
 */
public class BeanFactoryActivator implements BundleActivator {

  @Override
  public void start( BundleContext bundleContext ) throws Exception {
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
  