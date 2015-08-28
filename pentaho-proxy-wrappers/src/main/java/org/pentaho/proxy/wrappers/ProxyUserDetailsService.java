package org.pentaho.proxy.wrappers;

import java.lang.reflect.Method;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyUserDetailsService implements UserDetailsService {
  private Object sourceObject; // Hold the object so it is not garbage collected
  Method loadUserByNameMethod;
  Class<?> loadUserByNameReturnType;

  public ProxyUserDetailsService( Object sourceObject ) {
    this.sourceObject = sourceObject;
    Class<? extends Object> clazz = sourceObject.getClass();
    loadUserByNameMethod = ReflectionUtils.findMethod( clazz, "loadUserByName", new Class[] { String.class } );
    loadUserByNameReturnType = loadUserByNameMethod.getReturnType();
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    UserDetails proxyUserDetails = null;
    Object result = ReflectionUtils.invokeMethod( loadUserByNameMethod, sourceObject, new Object[] { username } );
    proxyUserDetails = new ProxyUserDetails( result );
    return proxyUserDetails;
  }
}
