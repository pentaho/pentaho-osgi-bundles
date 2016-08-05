package org.pentaho.proxy.creators.grantedauthorities;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.proxy.creators.ProxyObjectBase;
import org.pentaho.proxy.creators.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class S4GrantedAuthorityProxyCreator implements IProxyCreator<GrantedAuthority> {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public boolean supports( Class aClass ) {
    // supports legacy spring.security 2.0.8 SecurityContext
    return ProxyUtils.isRecursivelySupported( "org.springframework.security.GrantedAuthority", aClass );
  }

  @Override public GrantedAuthority create( Object o ) {
    return new ProxyGrantedAuthority( o );
  }

  protected IProxyFactory getProxyFactory() {
    return ProxyUtils.getInstance().getProxyFactory();
  }

  private class ProxyGrantedAuthority extends ProxyObjectBase implements GrantedAuthority {

    /**
     *
     */
    private static final long serialVersionUID = 1603112902745163281L;

    private Method getAuthorityMethod;

    public ProxyGrantedAuthority( Object target ) {
      super(target);
    }

    @Override
    public String getAuthority() {
      try {

        if ( getAuthorityMethod == null ) {
          getAuthorityMethod = ProxyUtils.findMethodByName( baseTarget.getClass(), "getAuthority" );
        }

        return (String) getAuthorityMethod.invoke( baseTarget );

      } catch ( InvocationTargetException | IllegalAccessException e ) {
        logger.error( e.getMessage() , e );
      }

      return null;
    }


  }

}
