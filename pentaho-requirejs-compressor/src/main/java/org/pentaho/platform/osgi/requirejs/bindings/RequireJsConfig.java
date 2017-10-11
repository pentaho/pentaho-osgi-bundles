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
 * Copyright 2014 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.osgi.requirejs.bindings;

import java.util.HashMap;

/**
 * Created by nbaker on 10/1/14.
 */
public class RequireJsConfig {
  String baseUrl;
  HashMap<String, String> paths;
  HashMap<String, Shim> shim;
  int waitSeconds;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl( String baseUrl ) {
    this.baseUrl = baseUrl;
  }

  public HashMap<String, String> getPaths() {
    return paths;
  }

  public void setPaths( HashMap<String, String> paths ) {
    this.paths = paths;
  }

  public HashMap<String, Shim> getShim() {
    return shim;
  }

  public void setShim( HashMap<String, Shim> shim ) {
    this.shim = shim;
  }

  public int getWaitSeconds() {
    return waitSeconds;
  }

  public void setWaitSeconds( int waitSeconds ) {
    this.waitSeconds = waitSeconds;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof RequireJsConfig ) ) {
      return false;
    }

    RequireJsConfig requireJsConfig = (RequireJsConfig) o;

    if ( waitSeconds != requireJsConfig.waitSeconds ) {
      return false;
    }
    if ( baseUrl != null ? !baseUrl.equals( requireJsConfig.baseUrl ) : requireJsConfig.baseUrl != null ) {
      return false;
    }
    if ( paths != null ? !paths.equals( requireJsConfig.paths ) : requireJsConfig.paths != null ) {
      return false;
    }
    if ( shim != null ? !shim.equals( requireJsConfig.shim ) : requireJsConfig.shim != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = baseUrl != null ? baseUrl.hashCode() : 0;
    result = 31 * result + ( paths != null ? paths.hashCode() : 0 );
    result = 31 * result + ( shim != null ? shim.hashCode() : 0 );
    result = 31 * result + waitSeconds;
    return result;
  }
}
