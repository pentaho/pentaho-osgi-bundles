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
package org.pentaho.osgi.notification.api.match;

import org.pentaho.osgi.notification.api.MatchCondition;

import java.util.List;

/**
 * Created by bryan on 9/22/14.
 */
public class OrCondition implements MatchCondition {
  private final List<MatchCondition> delegates;

  public OrCondition( List<MatchCondition> delegates ) {
    this.delegates = delegates;
  }

  @Override public boolean matches( Object object ) {
    for ( MatchCondition delegate : delegates ) {
      if ( delegate.matches( object ) ) {
        return true;
      }
    }
    return false;
  }
}
