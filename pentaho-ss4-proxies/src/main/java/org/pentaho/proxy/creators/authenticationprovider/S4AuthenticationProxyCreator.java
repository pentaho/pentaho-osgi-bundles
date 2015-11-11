package org.pentaho.proxy.creators.authenticationprovider;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collection;

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
    } else if( "org.springframework.security.providers.anonymous.AnonymousAuthenticationToken".equals( className ) ) {

      Method getKeyHash = ReflectionUtils.findMethod( o.getClass(), "getKeyHash" );
      Method getDetails = ReflectionUtils.findMethod( o.getClass(), "getDetails" );
      Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal" );
      Method getAuthorities = ReflectionUtils.findMethod( o.getClass(), "getAuthorities" );

      try {

        getKeyHash.setAccessible( true );
        getDetails.setAccessible( true );
        getPrincipal.setAccessible( true );
        getAuthorities.setAccessible( true );

        Object keyHash = getKeyHash.invoke( o );
        Object details = getDetails.invoke( o );
        Object principal = getPrincipal.invoke( o );
        Object authoritiesObj = getAuthorities.invoke( o );

        Collection<SimpleGrantedAuthority> s4Authorities = new ArrayList<SimpleGrantedAuthority>();

        if( authoritiesObj != null && authoritiesObj instanceof Object[] ){

          for( Object authorityObj : ( Object[] ) authoritiesObj ){

            Method getAuthority = ReflectionUtils.findMethod( authorityObj.getClass(), "getAuthority" );
            Object authority = getAuthority.invoke( authorityObj );
            if( authority != null ) {
              s4Authorities.add( new SimpleGrantedAuthority( authority.toString() ) );
            }
          }
        }

        AnonymousAuthenticationToken anonymousToken =
            new AnonymousAuthenticationToken( keyHash.toString(), principal, s4Authorities );
        anonymousToken.setDetails( details );

        return anonymousToken;

      } catch ( IllegalAccessException | InvocationTargetException e ) {
        logger.error( e.getMessage(), e );
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
