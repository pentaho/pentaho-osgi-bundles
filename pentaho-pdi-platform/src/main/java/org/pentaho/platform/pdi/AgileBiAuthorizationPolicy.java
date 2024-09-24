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
