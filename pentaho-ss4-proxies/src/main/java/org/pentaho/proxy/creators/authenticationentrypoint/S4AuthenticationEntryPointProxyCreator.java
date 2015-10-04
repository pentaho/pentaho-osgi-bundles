package org.pentaho.proxy.creators.authenticationentrypoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4AuthenticationEntryPointProxyCreator implements IProxyCreator<AuthenticationEntryPoint> {

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.ui.AuthenticationEntryPoint", aClass );
  }

  @Override public AuthenticationEntryPoint create( Object o ) {
    return new ProxyAuthenticationEntryPoint( o );
  }

  private class ProxyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private Object target;

    private Method commenceMethod;

    public ProxyAuthenticationEntryPoint( Object target ) {
      this.target = target;
    }

    @Override public void commence( HttpServletRequest request, HttpServletResponse response, AuthenticationException aex )
        throws IOException, ServletException {

      try {

        Object aexProxy = ProxyUtils.getInstance().getProxyFactory().createProxy( aex );

        if( commenceMethod == null ){

          commenceMethod = ProxyUtils.findMethodByName( target.getClass(), "commence",
              javax.servlet.ServletRequest.class,
              javax.servlet.ServletResponse.class,
              aexProxy.getClass() );
        }

        commenceMethod.invoke( target,
            ( javax.servlet.ServletRequest ) request ,
            ( javax.servlet.ServletResponse ) response,
            aexProxy );

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        e.printStackTrace();
      }
    }
  }
}
