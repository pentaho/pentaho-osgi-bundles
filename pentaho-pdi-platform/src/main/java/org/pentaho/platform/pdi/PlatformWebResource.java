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
