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

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nbaker on 8/31/15.
 */
public class MemoryUserRoleListService implements IUserRoleListService {

  private Map<String, String> userDefMap;

  public MemoryUserRoleListService( Map<String, String> userDefMap ) {
    this.userDefMap = userDefMap;
  }

  @Override public List<String> getAllRoles() {
    Set<String> roles = new HashSet<>();
    for ( Map.Entry<String, String> stringStringEntry : userDefMap.entrySet() ) {
      String[] split = stringStringEntry.getValue().split( "," );
      roles.addAll( Arrays.asList( split ) );
    }
    return new ArrayList<>( roles );
  }

  @Override public List<String> getSystemRoles() {
    return getAllRoles();
  }

  @Override public List<String> getAllRoles( ITenant iTenant ) {
    return getAllRoles();
  }

  @Override public List<String> getAllUsers() {

    Set<String> users = new HashSet<>();
    for ( Map.Entry<String, String> stringStringEntry : userDefMap.entrySet() ) {
      users.add( stringStringEntry.getKey() );
    }
    return new ArrayList<>( users );
  }

  @Override public List<String> getAllUsers( ITenant iTenant ) {
    return getAllUsers();
  }

  @Override public List<String> getUsersInRole( ITenant iTenant, String s ) {
    return Collections.emptyList();
  }

  @Override public List<String> getRolesForUser( ITenant iTenant, String s ) {
    String s1 = userDefMap.get( s );
    if( s1 == null ){
      return Collections.emptyList();
    }
    return Arrays.asList( s1.split( "," ) );
  }
}
