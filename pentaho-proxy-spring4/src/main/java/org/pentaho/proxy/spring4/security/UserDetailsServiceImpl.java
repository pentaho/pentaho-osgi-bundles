/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.proxy.spring4.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
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
