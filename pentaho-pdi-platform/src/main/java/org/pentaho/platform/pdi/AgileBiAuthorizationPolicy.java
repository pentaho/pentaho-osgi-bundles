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

package org.pentaho.platform.pdi;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * Placeholder Authorization policy. As there is no security in the AgileBI platform running in PDI, we always respond
 * with true.
 * <p>
 * Created by nbaker on 10/1/16.
 */
public class AgileBiAuthorizationPolicy implements IAuthorizationPolicy {

  private static final List<String> ALLOWED_ACTIONS =
    Arrays.asList( "org.pentaho.repository.read", "org.pentaho.repository.create",
      "org.pentaho.security.administerSecurity" );

  @Override public boolean isAllowed( String s ) {
    return true;
  }

  @Override public List<String> getAllowedActions( String s ) {
    return ALLOWED_ACTIONS;
  }

}
