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

import java.util.HashMap;
import java.util.Map;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;

/**
 * Created by tkafalas on 8/24/15.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
  HashMap<String, UserDetailsImpl> userDetailsList = new HashMap<String, UserDetailsImpl>();
  ITenantedPrincipleNameResolver tenantedPrincipleNameResolver;

  public UserDetailsServiceImpl( ITenantedPrincipleNameResolver tenantedPrincipleNameResolver,
      Map<String, String> userDefMap ) {
    this.tenantedPrincipleNameResolver = tenantedPrincipleNameResolver;

    for ( String username : userDefMap.keySet() ) {

      UserAttributeEditor userAttributeEditor = new UserAttributeEditor();
      userAttributeEditor.setAsText( userDefMap.get( username ) );
      userDetailsList.put( username, new UserDetailsImpl( username, (UserAttribute) userAttributeEditor
          .getValue() ) );
    }
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
    String principalId = tenantedPrincipleNameResolver.getPrincipleName( username );
    if ( userDetailsList.get( principalId ) == null ) {
      throw new UsernameNotFoundException( "user not found" );
    }
    return userDetailsList.get( principalId );
  }

}
