/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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

  @Override public String toString() {
    return getUsername();
  }
}
