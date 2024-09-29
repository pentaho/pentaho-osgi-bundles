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
