package org.pentaho.proxy.wrappers;

import java.lang.reflect.Method;

import org.springframework.security.GrantedAuthority;
import org.springframework.util.ReflectionUtils;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyGrantedAuthority implements GrantedAuthority {
  private static final long serialVersionUID = 1L;
  private Object sourceObject;
  Method getAuthorityMethod;
  Method compareToMethod;

  public ProxyGrantedAuthority( Object sourceObject ) {
    this.sourceObject = sourceObject;
    Class<? extends Object> clazz = sourceObject.getClass();
    getAuthorityMethod = ReflectionUtils.findMethod( clazz, "getAuthority" );
    compareToMethod = ReflectionUtils.findMethod( clazz, "compareTo", new Class[] { Object.class } );
  }

  @Override
  public int compareTo( Object o ) {
    if ( o != null && o instanceof GrantedAuthority ) {
      String rhsRole = ( (GrantedAuthority) o ).getAuthority();

      if ( rhsRole == null ) {
        return -1;
      }
      return getAuthority().compareTo( rhsRole );
    }
    return -1;
  }

  @Override
  public String getAuthority() {
    return (String) ReflectionUtils.invokeMethod( getAuthorityMethod, sourceObject );
  }

}
