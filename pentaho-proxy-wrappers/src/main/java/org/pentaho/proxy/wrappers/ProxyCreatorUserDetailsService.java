package org.pentaho.proxy.wrappers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.proxy.api.IProxyCreator;
import org.springframework.security.userdetails.UserDetailsService;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyCreatorUserDetailsService implements IProxyCreator<UserDetailsService> {
  private static final String[] supportedClasses =
      { "org.springframework.security.core.userdetails.UserDetailsService" };

  @SuppressWarnings( "unused" )
  private Object UserDetailService4; //Keep instance so it is not garbage collected

  public ProxyCreatorUserDetailsService( ) {
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
    if ( !supports( target.getClass() ) ) {
      throw new IllegalArgumentException( "This Proxy creator does not support an object of type "
          + target.getClass().getName() );
    }
    
    UserDetailsService userDetailsService = new ProxyUserDetailsService( target );
    return userDetailsService;
  }

  // TODO: delete this method after you know you don't need it
  private static List<Class> getSuperClasses( Object o ) {
    List<Class> classList = new ArrayList<Class>();
    Class clazz = o.getClass();
    Class superclass = clazz.getSuperclass();
    classList.add( superclass );
    while ( superclass != null ) {
      clazz = superclass;
      superclass = clazz.getSuperclass();
      classList.add( superclass );
    }
    return classList;
  }

}
