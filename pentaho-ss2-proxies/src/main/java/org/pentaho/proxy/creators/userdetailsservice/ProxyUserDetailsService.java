package org.pentaho.proxy.creators.userdetailsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created by tkafalas on 8/24/15.
 */
public class ProxyUserDetailsService implements UserDetailsService {
  private Logger logger = LoggerFactory.getLogger( getClass() );

  private String FULL_NAME_SS4_USERNOTFOUNDEXCEPTION = "org.springframework.security.core.userdetails.UsernameNotFoundException";

  private Object sourceObject; // Hold the object so it is not garbage collected
  Method loadUserByNameMethod;
  Class<?> loadUserByNameReturnType;

  public ProxyUserDetailsService( Object sourceObject ) {
    this.sourceObject = sourceObject;
    Class<? extends Object> clazz = sourceObject.getClass();
    loadUserByNameMethod = ReflectionUtils.findMethod( clazz, "loadUserByUsername", new Class[] { String.class } );
    loadUserByNameReturnType = loadUserByNameMethod.getReturnType();
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    try {
      Object result = ReflectionUtils.invokeMethod( loadUserByNameMethod, sourceObject, new Object[] { username } );
      if ( result != null ) {
        return new ProxyUserDetails( result );
      } else {
        logger.warn( "Got a null from calling the method loadUserByUsername( String username ) of UserDetailsService: "
            + sourceObject
            + ". This is an interface violation beacuse it is specified that loadUserByUsername method should never return null. Throwing a UsernameNotFoundException." );
      }
    } catch ( Exception e ) {
      if ( e.getClass().getName().equals( FULL_NAME_SS4_USERNOTFOUNDEXCEPTION ) ) {
        throw new UsernameNotFoundException( username + " not found", e );
      } else {
        logger.error( e.getMessage(), e );
      }
    }
    throw new UsernameNotFoundException( username );
  }
}
