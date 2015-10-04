package org.pentaho.proxy.creators;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.proxy.creators.authenticationentrypoint.AuthenticationEntryPointProxyCreator;
import org.pentaho.proxy.creators.authenticationentrypoint.AuthenticationExceptionProxyCreator;
import org.pentaho.proxy.creators.authenticationmanager.AuthenticationManagerProxyCreator;
import org.pentaho.proxy.creators.authenticationprovider.AuthenticationProviderProxyCreator;
import org.pentaho.proxy.creators.authenticationprovider.AuthenticationProxyCreator;
import org.pentaho.proxy.creators.userdetailsservice.UserDetailsServiceCreator;

/**
 * Created by nbaker on 9/1/15.
 */
public class ProxyCreatorActivator implements BundleActivator {
  @Override public void start( BundleContext bundleContext ) throws Exception {
    bundleContext.registerService( IProxyCreator.class, new UserDetailsServiceCreator(), null );
    bundleContext.registerService( IProxyCreator.class, new AuthenticationProviderProxyCreator(), null );
    bundleContext.registerService( IProxyCreator.class, new AuthenticationProxyCreator(), null );

    bundleContext.registerService( IProxyCreator.class, new AuthenticationEntryPointProxyCreator(), null );
    bundleContext.registerService( IProxyCreator.class, new AuthenticationExceptionProxyCreator(), null );
    bundleContext.registerService( IProxyCreator.class, new AuthenticationManagerProxyCreator(), null );
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }
}
