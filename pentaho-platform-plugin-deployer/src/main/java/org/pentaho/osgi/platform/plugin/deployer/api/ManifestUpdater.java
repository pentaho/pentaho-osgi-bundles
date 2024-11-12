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

package org.pentaho.osgi.platform.plugin.deployer.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * Created by bryan on 8/26/14.
 */
public interface ManifestUpdater {

  Map<String, String> getImports();

  Set<String> getExportServices();

  void write( Manifest originalManifest, OutputStream outputStream, String name,
                     String symbolicName, String version ) throws IOException;

  void addEntry( Object key, Object value );

  void setBundleSymbolicName( String name );


  String getBundleSymbolicName();
}
