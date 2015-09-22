package org.pentaho.caching.ehcache;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by nbaker on 9/17/15.
 */
public class Activator implements BundleActivator {
  @Override public void start( BundleContext bundleContext ) throws Exception {
    // Nothing to do here
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {
    // Make sure the EhCache CacheManager is shutdown
    EhcacheProvidingService.getCacheManager().shutdown();
  }
}
