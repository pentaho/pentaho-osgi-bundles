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

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by nbaker on 10/1/16.
 */
public class AgileBiAuthorizationPolicyTest {

  private AgileBiAuthorizationPolicy authorizationPolicy = new AgileBiAuthorizationPolicy();

  @Test
  public void isAllowed() throws Exception {
    assertThat( authorizationPolicy.isAllowed( "anything" ), is( true ) );
  }

  @Test
  public void getAllowedActions() throws Exception {
    assertThat( authorizationPolicy.getAllowedActions( "anything" ), equalTo( Arrays
      .asList( "org.pentaho.repository.read", "org.pentaho.repository.create",
        "org.pentaho.security.administerSecurity" )));
  }

}