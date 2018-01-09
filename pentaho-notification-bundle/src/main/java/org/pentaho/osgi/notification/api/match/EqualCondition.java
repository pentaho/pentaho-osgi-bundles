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

/**
 * Created by bryan on 9/22/14.
 */
public class EqualCondition implements MatchCondition {
  private final Object predicate;

  public EqualCondition( Object predicate ) {
    this.predicate = predicate;
  }

  @Override public boolean matches( Object object ) {
    if ( predicate == null ) {
      return object == null;
    } else {
      return object != null && predicate.equals( object );
    }
  }
}
