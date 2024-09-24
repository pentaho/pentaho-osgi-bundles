package org.pentaho.osgi.springsecurity;

import org.pentaho.osgi.api.IAuthenticationProviderProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created by nbaker on 4/20/15.
 */
public class AuthenticationProviderProxy implements IAuthenticationProviderProxy {

  private AuthenticationManager authenticationManager;
  private static AuthenticationProviderProxy INSTANCE = new AuthenticationProviderProxy();

  private AuthenticationProviderProxy() {

  }

  public static AuthenticationProviderProxy getInstance(){
    return INSTANCE;
  }

  public void setAuthenticationManager(
      AuthenticationManager authenticationManager ) {
    this.authenticationManager = authenticationManager;
  }

  @Override public Object authenticate( Object o ) { // o is an Authentication object
    Method getPrincipal = ReflectionUtils.findMethod( o.getClass(), "getPrincipal");
    Method getCredentials = ReflectionUtils.findMethod( o.getClass(), "getCredentials" );

    Object credentials = ReflectionUtils.invokeMethod( getCredentials, o );
    Object principal = ReflectionUtils.invokeMethod( getPrincipal, o );
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken( principal, credentials );

    return authenticationManager.authenticate( token );

  }

}
