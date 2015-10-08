package org.pentaho.proxy.creators.authenticationprovider;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.util.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

  private class ProxyAuthenticationProvider implements AuthenticationProvider {

    private Object target;

    public ProxyAuthenticationProvider( Object target ) {
      this.target = target;
    }

    private Method findMethodByName( Class clazz, String methodName, Class... arguments ){
      for ( Method method : clazz.getMethods() ) {
        if( method.getName().equals( methodName ) ){
          for ( int i = 0; i < arguments.length; i++ ) {
            if( method.getParameterTypes()[ i ].isAssignableFrom( arguments[i] ) == false ){
              return null;
            }
          }
          return method;
        }
      }
      return null;
    }

    @Override public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

      try {
        Object auth = getFactory().createProxy( authentication );
        Method authenticate = findMethodByName( target.getClass(), "authenticate", auth.getClass() );
        Object retVal = authenticate.invoke( target, auth );

        if ( retVal != null ) {
          return getFactory().createProxy( retVal );
        }
      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage() , e );
      }
      return null;

    }

    @Override public boolean supports( Class aClass ) {
      return true;
    }
  }

}
