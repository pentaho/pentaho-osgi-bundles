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

package org.pentaho.proxy.spring4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.proxy.spring4.security.UserDetailsServiceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by tkafalas on 8/24/15.
 */
public class UserDetailsServiceImplTest {
  @Test
  public void test() {
    HashMap<String, String> userDefMap = new HashMap<String, String>();
    userDefMap.put( "admin", "password,Administrator,Authenticated" );
    userDefMap.put( "suzy", "password,Power User,Authenticated" );
    userDefMap.put( "pat", "password,Business Analyst,Authenticated" );
    userDefMap.put( "tiffany", "differentpassword,Report Author,Authenticated" );

    ITenantedPrincipleNameResolver mockTenantedPrincipleNameResolver = new MockITenantedPrincipleNameResolver();

    UserDetailsServiceImpl userDetailsServiceImpl =
        new UserDetailsServiceImpl( mockTenantedPrincipleNameResolver, userDefMap );

    // check admin
    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername( "admin" );
    assertNotNull( userDetails );
    assertEquals( "password", userDetails.getPassword() );
    checkAuthorities( userDetails, new String[] { "Administrator", "Authenticated" } );

    // check tiffany
    userDetails = userDetailsServiceImpl.loadUserByUsername( "tiffany" );
    assertNotNull( userDetails );
    assertEquals( "differentpassword", userDetails.getPassword() );
    checkAuthorities( userDetails, new String[] { "Report Author", "Authenticated" } );

  }

  private void checkAuthorities( UserDetails userDetails, String[] authorityNames ) {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertEquals( authorityNames.length, authorities.size() );
    for ( String authorityName : authorityNames ) {
      boolean found = false;
      for ( GrantedAuthority authority : authorities ) {
        if ( authorityName.equals( authority.getAuthority() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( "Authority " + authorityName + " not found", found );
    }
  }

  public class MockITenantedPrincipleNameResolver implements ITenantedPrincipleNameResolver {

    @Override
    public String getPrincipleId( ITenant tenant, String principalName ) {
      return principalName + "-/pentaho/tenant0";
    }

    @Override
    public String getPrincipleName( String principalName ) {
      return principalName;
    }

    @Override
    public ITenant getTenant( String arg0 ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isValid( String arg0 ) {
      // TODO Auto-generated method stub
      return false;
    }

  }
}
