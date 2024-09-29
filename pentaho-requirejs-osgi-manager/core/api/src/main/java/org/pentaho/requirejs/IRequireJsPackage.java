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

package org.pentaho.requirejs;

import java.net.URL;
import java.util.Map;

public interface IRequireJsPackage {
  String getName();
  String getVersion();

  String getWebRootPath();

  boolean preferGlobal();

  Map<String, String> getModules();
  String getModuleMainFile( String moduleId );

  Map<String, String> getDependencies();

  Map<String, Map<String, ?>> getConfig();
  Map<String, Map<String, String>> getMap();
  Map<String, Map<String, ?>> getShim();

  boolean hasScript( String name );
  URL getScriptResource( String name );
}
