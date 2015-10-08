package org.pentaho.proxy.creators.authenticationprovider;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by nbaker on 8/31/15.
 */
public class S4AuthenticationProxyCreator implements IProxyCreator<Authentication> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  private IProxyFactory iProxyFactory;

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.Authentication", aClass );
  }

  @Override public Authentication create( Object o ) {
    String className = o.getClass().getName();
    if( "org.springframework.security.providers.UsernamePasswordAuthenticationToken".equals( className ) ){
      Method getCredentials = ReflectionUtils.findMethod( o.getClass(), "getCredentials" );
      Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal" );

      try {
        Object credentials = getCredentials.invoke( o, new Object[] {} );
        Object principal = getPrincipal.invoke( o, new Object[] {} );
        return new UsernamePasswordAuthenticationToken( principal, credentials );
      } catch ( IllegalAccessException e ) {
        logger.error( e.getMessage(), e );
      } catch ( InvocationTargetException e ) {
        logger.error( e.getMessage() , e );
      }
    }
    return null;
  }

  private IProxyFactory getFactory(){
    if( iProxyFactory == null ){
      iProxyFactory = PentahoSystem.get( IProxyFactory.class );
    }
    return iProxyFactory;
  }

}
