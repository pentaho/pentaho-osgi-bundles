package org.pentaho.proxy.creators.authenticationmanager;

import java.io.IOException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4AuthenticationManagerProxyCreator implements IProxyCreator<AuthenticationManager> {

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.AuthenticationManager", aClass );
  }

  @Override public AuthenticationManager create( Object o ) {
    return new ProxyAuthenticationManager( o );
  }

  private class ProxyAuthenticationManager implements AuthenticationManager {

    private Object target;

    public ProxyAuthenticationManager( Object target ) {
      this.target = target;
    }

    @Override public Authentication authenticate( Authentication authentication ) throws AuthenticationException {

      try {
        Object auth = ProxyUtils.getInstance().getProxyFactory().createProxy( authentication );
        Method authenticate = ProxyUtils.findMethodByName( target.getClass(), "authenticate", auth.getClass() );
        Object retVal = authenticate.invoke( target, auth );

        if ( retVal != null ){
          return ProxyUtils.getInstance().getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        e.printStackTrace();
      }

      return null;
    }
  }
}
