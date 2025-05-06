/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
