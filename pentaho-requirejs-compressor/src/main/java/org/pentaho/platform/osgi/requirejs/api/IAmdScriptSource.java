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


package org.pentaho.platform.osgi.requirejs.api;


// import org.osgi.framework.Version;
import org.pentaho.platform.osgi.requirejs.bindings.RequireJsConfig;

/**
 * Created by nbaker on 10/1/14.
 */
public interface IAmdScriptSource {
  String getName();
  // Version getVersion();
  RequireJsConfig getRequireJsConfig();

}
