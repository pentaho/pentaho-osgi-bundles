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

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acl.AclEntry;

/**
 * The "Platform" when running inside of PDI lacks security. We supply this AclVoter to satisfy the need for an
 * IAclVoter in the system.
 * <p>
 * This implementation is completely open, nothing is vetoed.
 * <p>
 * Created by nbaker on 8/15/16.
 */
public class AgileBiAclVoter implements IAclVoter {
  @Override public boolean hasAccess( IPentahoSession iPentahoSession, IAclHolder iAclHolder, int i ) {
    return true;
  }

  @Override public AclEntry[] getEffectiveAcls( IPentahoSession iPentahoSession, IAclHolder iAclHolder ) {
    return iAclHolder.getEffectiveAccessControls().toArray( new AclEntry[] {} );
  }

  @Override public boolean isPentahoAdministrator( IPentahoSession iPentahoSession ) {
    return true;
  }

  @Override public GrantedAuthority getAdminRole() {
    return new GrantedAuthorityImpl( "admin" );
  }

  @Override public void setAdminRole( GrantedAuthority grantedAuthority ) {

  }

  @Override public boolean isGranted( IPentahoSession iPentahoSession, GrantedAuthority grantedAuthority ) {
    return true;
  }

  @Override public IPentahoAclEntry getEffectiveAcl( IPentahoSession iPentahoSession, IAclHolder iAclHolder ) {
    return iAclHolder.getEffectiveAccessControls().get( 0 );
  }
}
