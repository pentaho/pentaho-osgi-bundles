package org.pentaho.proxy.creators.authenticationentrypoint;

import org.pentaho.proxy.creators.ProxyUtils;
import org.pentaho.proxy.creators.authenticationprovider.S4AuthenticationProxyCreator;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4AuthenticationExceptionProxyCreator implements IProxyCreator<AuthenticationException> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.AuthenticationException", aClass );
  }

  @Override public AuthenticationException create( Object o ) {
    return new ProxyAuthenticationException( null, o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyAuthenticationException extends AuthenticationException {

    private Object target;

    private Method getAuthenticationMethod;
    private Method getExtraInformationMethod;

    public ProxyAuthenticationException( String msg, Object target ) {
      super( msg );
      this.target = target;
    }

    @Override public Authentication getAuthentication() {

      try {

        if ( getAuthenticationMethod == null ) {
          getAuthenticationMethod = ProxyUtils.findMethodByName( target.getClass(), "getAuthentication" );
        }

        Object retVal = getAuthenticationMethod.invoke( target );

        if ( retVal != null ) {
          return getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage(), e );
      }

      return null;
    }

    @Override public Object getExtraInformation() {

      try {

        if ( getExtraInformationMethod == null ) {
          getExtraInformationMethod = ProxyUtils.findMethodByName( target.getClass(), "getExtraInformation" );
        }

        return getExtraInformationMethod.invoke( target );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage() , e );
      }

      return null;
    }
  }
}
