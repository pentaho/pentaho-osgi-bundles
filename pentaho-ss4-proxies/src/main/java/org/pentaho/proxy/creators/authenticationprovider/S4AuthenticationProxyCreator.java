package org.pentaho.proxy.creators.authenticationprovider;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by nbaker on 8/31/15.
 */
public class S4AuthenticationProxyCreator implements IProxyCreator<Authentication> {

  private IProxyFactory iProxyFactory;

  @Override public boolean supports( Class aClass ) {
    return "org.springframework.security.Authentication".equals( aClass.getName() );
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
        e.printStackTrace();
      } catch ( InvocationTargetException e ) {
        e.printStackTrace();
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
