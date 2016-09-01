/*!
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
 * Copyright (c) 2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.pdi;

import org.junit.Test;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acl.AclEntry;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 8/15/16.
 */
public class AgileBiAclVoterTest {
  AgileBiAclVoter aclVoter = new AgileBiAclVoter();

  @Test
  public void hasAccess() throws Exception {
    assertTrue( aclVoter.hasAccess( null, null, 0 ) );
  }

  @Test
  public void getEffectiveAcls() throws Exception {
    IAclHolder aclHolder = mock( IAclHolder.class );
    IPentahoAclEntry acl = mock( IPentahoAclEntry.class );
    List<IPentahoAclEntry> acls = Collections.singletonList( acl );
    when( aclHolder.getEffectiveAccessControls() ).thenReturn( acls );
    AclEntry[] effectiveAcls = aclVoter.getEffectiveAcls( null, aclHolder );
    assertNotNull( effectiveAcls );
    assertEquals( 1, effectiveAcls.length );
    assertSame( acl, effectiveAcls[ 0 ] );
  }

  @Test
  public void isPentahoAdministrator() throws Exception {
    assertTrue( aclVoter.isPentahoAdministrator( null ) );
  }

  @Test
  public void getAdminRole() throws Exception {
    assertEquals( "admin", aclVoter.getAdminRole().getAuthority() );
  }

  @Test
  public void setAdminRole() throws Exception {
    aclVoter.setAdminRole( new GrantedAuthorityImpl( "bogus" ) );
    // set was ignored
    assertEquals( "admin", aclVoter.getAdminRole().getAuthority() );
  }

  @Test
  public void isGranted() throws Exception {
    assertTrue( aclVoter.isGranted( null, mock( GrantedAuthority.class ) ) );
  }

  @Test
  public void getEffectiveAcl() throws Exception {
    IAclHolder aclHolder = mock( IAclHolder.class );
    IPentahoAclEntry acl = mock( IPentahoAclEntry.class );
    List<IPentahoAclEntry> acls = Collections.singletonList( acl );
    when( aclHolder.getEffectiveAccessControls() ).thenReturn( acls );
    AclEntry effectiveAcl = aclVoter.getEffectiveAcl( null, aclHolder );
    assertSame( acl, effectiveAcl );
  }

}