package org.pentaho.proxy.creators.grantedauthorities;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.GrantedAuthority;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GrantedAuthorityProxyCreator implements IProxyCreator<GrantedAuthority> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public boolean supports( Class aClass ) {
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.core.GrantedAuthority", aClass );
  }

  @Override public GrantedAuthority create( Object o ) {
    return new ProxyGrantedAuthority( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyGrantedAuthority implements GrantedAuthority {

    /**
     *
     */
    private static final long serialVersionUID = 1603112902745163281L;

    private Object target;

    private Method getAuthorityMethod;

    public ProxyGrantedAuthority( Object target ) {
      this.target = target;
    }

    @Override
    public String getAuthority() {
      try {

        if ( getAuthorityMethod == null ) {
          getAuthorityMethod = ProxyUtils.findMethodByName( target.getClass(), "getAuthority" );
        }

        return (String) getAuthorityMethod.invoke( target );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage() , e );
      }

      return null;
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
  }

}
