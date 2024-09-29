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
