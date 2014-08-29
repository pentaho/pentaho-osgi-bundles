/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.pentaho.osgi.platform.plugin.deployer.PlatformPluginBundlingURLConnection;
import org.pentaho.osgi.platform.plugin.deployer.api.ManifestUpdater;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bryan on 8/26/14.
 */
public class ManifestUpdaterImpl implements ManifestUpdater {
  private final Map<String, String> imports;
  private final Set<String> exportServices;

  public ManifestUpdaterImpl() {
    imports = new HashMap<String, String>();
    exportServices = new HashSet<String>();
  }

  @Override public Map<String, String> getImports() {
    return imports;
  }

  @Override public Set<String> getExportServices() {
    return exportServices;
  }

  @Override public void write( java.util.jar.Manifest originalManifest, OutputStream outputStream, String name,
                               String symbolicName, String version ) throws IOException {
    java.util.jar.Manifest newManifest = new java.util.jar.Manifest(  );
    if ( originalManifest != null ) {
      newManifest = new java.util.jar.Manifest( originalManifest );
    }
    newManifest.getMainAttributes().putValue( "Manifest-Version", "2" );
    newManifest.getMainAttributes().putValue( "Bundle-ManifestVersion", "2" );
    newManifest.getMainAttributes().putValue( "Bundle-SymbolicName", symbolicName );
    newManifest.getMainAttributes().putValue( "Bundle-Name", name );
    newManifest.getMainAttributes().putValue( "Bundle-Version", version );
    newManifest.getMainAttributes().putValue( "Bundle-Blueprint", PluginZipFileProcessor.BLUEPRINT );
    newManifest.getMainAttributes()
      .putValue( "Export-Service", join( getExportServices(), "," ) );
    newManifest.getMainAttributes()
      .putValue( "Import-Package", getImportString( ) );
    newManifest.write( outputStream );
  }

  protected String join( Collection<String> collection, String delimiter ) {
    StringBuilder sb = new StringBuilder();
    for ( String value : collection ) {
      sb.append( value );
      sb.append( delimiter );
    }
    if ( sb.length() > 0 ) {
      sb.setLength( sb.length() - delimiter.length() );
    }
    return sb.toString();
  }

  protected String getImportString( ) {
    Set<String> imports = new HashSet<String>();
    for ( Map.Entry<String, String> entry : getImports().entrySet() ) {
      String importString = entry.getKey();
      String version = entry.getValue();
      if ( version != null ) {
        importString += ";version=\"" + entry.getValue() + "\"";
      }
      imports.add( importString );
    }
    List<String> importList = new ArrayList<String>( imports );
    Collections.sort( importList );
    return join( importList, "," );
  }
}
