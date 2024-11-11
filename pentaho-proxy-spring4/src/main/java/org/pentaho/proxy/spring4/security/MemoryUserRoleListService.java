/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
    if ( s1 == null ) {
      return Collections.emptyList();
    }
    return Arrays.asList( s1.split( "," ) );
  }
}
