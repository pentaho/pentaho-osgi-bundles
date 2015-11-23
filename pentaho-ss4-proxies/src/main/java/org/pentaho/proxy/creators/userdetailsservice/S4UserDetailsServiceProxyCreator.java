package org.pentaho.proxy.creators.userdetailsservice;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4UserDetailsServiceProxyCreator implements IProxyCreator<UserDetailsService> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public boolean supports( Class<?> clazz ) {
    // supports legacy spring.security 2.0.8 UserDetailsService
    return "org.springframework.security.userdetails.UserDetailsService".equals( clazz.getName() );
  }

  @Override public UserDetailsService create( Object target ) {
    return new S4UserDetailsServiceProxy( target );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class S4UserDetailsServiceProxy implements UserDetailsService {

    private Object target;

    private Method loadUserByUsernameMethod;

    public S4UserDetailsServiceProxy( Object target ){
      this.target = target;
    }

    @Override public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {

      if( loadUserByUsernameMethod == null ){
        loadUserByUsernameMethod = ReflectionUtils.findMethod( target.getClass(), "loadUserByUsername", String.class );
      }

      try {

        Object retVal = loadUserByUsernameMethod.invoke( target, username );

        if( retVal != null ) {
          return getProxyFactory().createProxy( retVal );
        }

      } catch ( InvocationTargetException | IllegalAccessException | ProxyException e ) {
        logger.error( e.getMessage() , e );
      }

      return null;
    }
  }
}
