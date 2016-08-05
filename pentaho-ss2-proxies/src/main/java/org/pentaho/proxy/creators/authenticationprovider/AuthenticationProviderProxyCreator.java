package org.pentaho.proxy.creators.authenticationprovider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;

/**
 * Created by nbaker on 8/31/15.
 */
public class AuthenticationProviderProxyCreator implements IProxyCreator<AuthenticationProvider> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  private IProxyFactory iProxyFactory;

  @Override public boolean supports( Class aClass ) {
    return "org.springframework.security.authentication.AuthenticationProvider".equals( aClass.getName() );
  }

  @Override public AuthenticationProvider create( Object o ) {
    return new ProxyAuthenticationProvider( o );
  }

  private IProxyFactory getFactory(){
    if( iProxyFactory == null ){
      iProxyFactory = PentahoSystem.get( IProxyFactory.class );
    }
    return iProxyFactory;
  }

  private class ProxyAuthenticationProvider extends ProxyObjectBase implements AuthenticationProvider {


    public ProxyAuthenticationProvider( Object target ) {
      super(target);
    }

    @Override public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

      try {
        Object auth = getFactory().createProxy( authentication );
        Method authenticate = ProxyUtils.findMethodByName( baseTarget.getClass(), "authenticate", auth.getClass() );
        Object retVal = authenticate.invoke( baseTarget, auth );

        if ( retVal != null ) {
          return getFactory().createProxy( retVal );
        }
      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage() , e );
      }
      return null;

    }


    //TODO: The call to supports should be proxied to the target object
    @Override public boolean supports( Class aClass ) {
      return true;
    }
  }

}
