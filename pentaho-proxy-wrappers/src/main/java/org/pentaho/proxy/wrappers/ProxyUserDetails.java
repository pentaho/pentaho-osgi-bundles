package org.pentaho.proxy.wrappers;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyUserDetails implements UserDetails {
  private static final long serialVersionUID = 1L;
  private Object sourceObject;
  Method getAuthoritiesMethod;
  Method getPasswordMethod;
  Method getUsernameMethod;
  Method isAccountNonExpiredMethod;
  Method isAccountNonLockedMethod;
  Method isCredentialsNonExpiredMethod;
  Method isEnabledMethod;

  Class<?> loadUserByNameReturnType;

  public ProxyUserDetails( Object sourceObject ) {
    this.sourceObject = sourceObject;
    Class<? extends Object> clazz = sourceObject.getClass();
    getAuthoritiesMethod = ReflectionUtils.findMethod( clazz, "getAuthorities" );
    getPasswordMethod = ReflectionUtils.findMethod( clazz, "getPassword" );
    getUsernameMethod = ReflectionUtils.findMethod( clazz, "getUsername" );
    isAccountNonExpiredMethod = ReflectionUtils.findMethod( clazz, "isAccountNonExpired" );
    isAccountNonLockedMethod = ReflectionUtils.findMethod( clazz, "isAccountNonLocked" );
    isCredentialsNonExpiredMethod = ReflectionUtils.findMethod( clazz, "isCredentialsNonExpired" );
    isEnabledMethod = ReflectionUtils.findMethod( clazz, "isEnabled" );
  }

  @Override
  public GrantedAuthority[] getAuthorities() {
    @SuppressWarnings( "unchecked" )
    Collection<? extends Object> source =
        (Collection<? extends Object>) ReflectionUtils.invokeMethod( getAuthoritiesMethod, sourceObject );
    GrantedAuthority[] result = new GrantedAuthority[source.size()];
    int i = 0;
    for ( Object o : source ) {
      GrantedAuthority grantedAuthority = new ProxyGrantedAuthority( o );
      result[i++] = grantedAuthority;
    }
    return result;
  }

  @Override
  public String getPassword() {
    String result = (String) ReflectionUtils.invokeMethod( getPasswordMethod, sourceObject );
    return result;
  }

  @Override
  public String getUsername() {
    return (String) ReflectionUtils.invokeMethod( getUsernameMethod, sourceObject );
  }

  @Override
  public boolean isAccountNonExpired() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonExpiredMethod, sourceObject );
  }

  @Override
  public boolean isAccountNonLocked() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonLockedMethod, sourceObject );
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonLockedMethod, sourceObject );
  }

  @Override
  public boolean isEnabled() {
    return (boolean) ReflectionUtils.invokeMethod( isEnabledMethod, sourceObject );
  }

}
