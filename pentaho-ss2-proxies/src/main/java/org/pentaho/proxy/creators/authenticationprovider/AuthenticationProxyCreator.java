package org.pentaho.proxy.creators.authenticationprovider;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.proxy.creators.ProxyUtils;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by nbaker on 8/31/15.
 */
public class AuthenticationProxyCreator implements IProxyCreator<Authentication> {
  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.core.Authentication", aClass );
  }

  @Override public Authentication create( Object o ) {
    String className = o.getClass().getName();

    if ( "org.springframework.security.authentication.UsernamePasswordAuthenticationToken".equals( className ) ) {
      Method getCredentials = ReflectionUtils.findMethod( o.getClass(), "getCredentials" );
      Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal" );
      Method getAuthorities = ReflectionUtils.findMethod( o.getClass(), "getAuthorities" );

      try {
        Object credentials = getCredentials.invoke( o, new Object[] {} );
        Object principal = getPrincipal.invoke( o, new Object[] {} );
        Collection granted = (Collection) getAuthorities.invoke( o, new Object[] {} );

        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();

        for ( Object oGrant : granted ) {
          Method getAuthority = ReflectionUtils.findMethod( oGrant.getClass(), "getAuthority" );
          Object auth = getAuthority.invoke( oGrant, new Object[] {} );
          authorityList.add( new GrantedAuthorityImpl( auth.toString() ) );
        }

        return new UsernamePasswordAuthenticationToken( principal, credentials,
            authorityList.toArray( new GrantedAuthority[ authorityList.size() ] ) );
      } catch ( IllegalAccessException e ) {
        e.printStackTrace();
      } catch ( InvocationTargetException e ) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
