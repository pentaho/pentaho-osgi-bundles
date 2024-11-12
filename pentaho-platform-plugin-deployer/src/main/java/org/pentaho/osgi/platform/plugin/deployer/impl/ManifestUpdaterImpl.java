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

package org.pentaho.osgi.platform.plugin.deployer.impl;

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
import java.util.jar.Attributes;

/**
 * Created by bryan on 8/26/14.
 */
public class ManifestUpdaterImpl implements ManifestUpdater {
  private final Map<String, String> imports;
  private final Set<String> exportServices;
  private final Map<Object, Object> entries = new HashMap<Object, Object>();
  private String bundleName;

  public ManifestUpdaterImpl() {
    imports = new HashMap<String, String>();
    exportServices = new HashSet<String>();
  }

  @Override public void setBundleSymbolicName( String name ) {
    bundleName = name;
  }

  @Override public String getBundleSymbolicName() {
    return bundleName;
  }

  @Override public Map<String, String> getImports() {
    return imports;
  }

  @Override public Set<String> getExportServices() {
    return exportServices;
  }

  @Override public void addEntry( Object key, Object value ) {
    entries.put( key, value.toString() );
  }

  @Override public void write( java.util.jar.Manifest originalManifest, OutputStream outputStream, String name,
                               String symbolicName, String version ) throws IOException {
    java.util.jar.Manifest newManifest = new java.util.jar.Manifest();
    if ( originalManifest != null ) {
      newManifest = new java.util.jar.Manifest( originalManifest );
    }

    Attributes mainAttributes = newManifest.getMainAttributes();
    for ( Map.Entry<Object, Object> entry : entries.entrySet() ) {
      mainAttributes.putValue( entry.getKey().toString(), entry.getValue().toString() );
    }
    mainAttributes.putValue( "Manifest-Version", "1.0" );
    mainAttributes.putValue( "Bundle-ManifestVersion", "2" );
    mainAttributes.putValue( "Bundle-SymbolicName", bundleName != null ? bundleName : symbolicName );
    mainAttributes.putValue( "Bundle-Name", name );
    mainAttributes.putValue( "Bundle-Version", version );
    //Custom attribute to recognize if this is platform plugin
    mainAttributes.putValue( "Bundle-PlatformPluginName", symbolicName );
    mainAttributes
        .putValue( "Export-Service", join( getExportServices(), "," ) );
    mainAttributes
        .putValue( "Import-Package", getImportString() );
    mainAttributes.putValue( "DynamicImport-Package", "*" );
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

  protected String getImportString() {
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
