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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pentaho.platform.api.engine.IPlatformWebResource;

/**
 * Simple bean implementing IPlatformWebResource
 *
 * Created by nbaker on 9/8/16.
 */
public class PlatformWebResource implements IPlatformWebResource {

  private final String context, location;

  public PlatformWebResource( String context, String location ) {
    this.context = context;
    this.location = location;
  }

  @Override public String getContext() {
    return context;
  }

  @Override public String getLocation() {
    return location;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }

    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    PlatformWebResource that = (PlatformWebResource) o;

    return new EqualsBuilder()
      .append( context, that.context )
      .append( location, that.location )
      .isEquals();
  }

  @Override public int hashCode() {
    return new HashCodeBuilder( 17, 37 )
      .append( context )
      .append( location )
      .toHashCode();
  }
}
