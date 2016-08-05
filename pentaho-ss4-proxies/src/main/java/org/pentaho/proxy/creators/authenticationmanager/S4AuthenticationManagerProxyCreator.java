package org.pentaho.proxy.creators.authenticationmanager;

import java.io.IOException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4AuthenticationManagerProxyCreator implements IProxyCreator<AuthenticationManager> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.AuthenticationManager", aClass );
  }

  @Override public AuthenticationManager create( Object o ) {
    return new ProxyAuthenticationManager( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyAuthenticationManager extends ProxyObjectBase implements AuthenticationManager {

    public ProxyAuthenticationManager( Object target ) {
      super(target);
    }

    @Override public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

      try {
        Object auth = getProxyFactory().createProxy( authentication );
        Method authenticate = ProxyUtils.findMethodByName( baseTarget.getClass(), "authenticate", auth.getClass() );
        Object retVal = authenticate.invoke( baseTarget, auth );

        if ( retVal != null ){
          return getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage() , e );
      }

      return null;
    }
  }
}
