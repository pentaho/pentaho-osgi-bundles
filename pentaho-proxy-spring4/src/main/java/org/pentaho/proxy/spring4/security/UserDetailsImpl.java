package org.pentaho.proxy.spring4.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.memory.UserAttribute;

/**
 * Created by tkafalas on 8/24/15.
 */
public class UserDetailsImpl extends User implements UserDetails {

  private static final long serialVersionUID = 1L;

  public UserDetailsImpl( String username, UserAttribute userAttribute ) {
    super( username, userAttribute.getPassword(), userAttribute.getAuthorities() );
  }
}
