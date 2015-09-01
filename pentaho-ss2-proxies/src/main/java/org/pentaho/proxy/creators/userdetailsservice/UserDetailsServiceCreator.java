package org.pentaho.proxy.creators.userdetailsservice;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.springframework.security.userdetails.UserDetailsService;

/**
 * Created by tkafalas on 8/24/15.
 */
public class UserDetailsServiceCreator implements IProxyCreator<UserDetailsService> {
  private static final String[] supportedClasses =
      { "org.springframework.security.core.userdetails.UserDetailsService" };

  @SuppressWarnings( "unused" )
  private Object UserDetailService4; //Keep instance so it is not garbage collected

  public UserDetailsServiceCreator() {
    System.out.println("********************************************************************************");
  }

  @Override
  public boolean supports( Class<?> clazz ) {
    for ( String className : supportedClasses ) {
      if ( clazz.getName().equals( className ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public UserDetailsService create( Object target ) {
    UserDetailsService userDetailsService = new ProxyUserDetailsService( target );
    return userDetailsService;
  }

}
