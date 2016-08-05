package org.pentaho.proxy.creators.userdetailsservice;

import java.lang.reflect.Method;
import java.util.Collection;

import org.pentaho.proxy.creators.ProxyObjectBase;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyUserDetails extends ProxyObjectBase implements UserDetails {
  private static final long serialVersionUID = 1L;
  Method getAuthoritiesMethod;
  Method getPasswordMethod;
  Method getUsernameMethod;
  Method isAccountNonExpiredMethod;
  Method isAccountNonLockedMethod;
  Method isCredentialsNonExpiredMethod;
  Method isEnabledMethod;

  Class<?> loadUserByNameReturnType;

  public ProxyUserDetails( Object sourceObject ) {
    super(sourceObject);
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
        (Collection<? extends Object>) ReflectionUtils.invokeMethod( getAuthoritiesMethod, baseTarget );
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
    String result = (String) ReflectionUtils.invokeMethod( getPasswordMethod, baseTarget );
    return result;
  }

  @Override
  public String getUsername() {
    return (String) ReflectionUtils.invokeMethod( getUsernameMethod, baseTarget );
  }

  @Override
  public boolean isAccountNonExpired() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonExpiredMethod, baseTarget );
  }

  @Override
  public boolean isAccountNonLocked() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonLockedMethod, baseTarget );
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return (boolean) ReflectionUtils.invokeMethod( isAccountNonLockedMethod, baseTarget );
  }

  @Override
  public boolean isEnabled() {
    return (boolean) ReflectionUtils.invokeMethod( isEnabledMethod, baseTarget );
  }

}
