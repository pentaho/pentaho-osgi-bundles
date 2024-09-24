/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
