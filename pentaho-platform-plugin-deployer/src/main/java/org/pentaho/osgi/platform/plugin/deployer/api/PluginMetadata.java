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

import org.w3c.dom.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by bryan on 8/26/14.
 */
public interface PluginMetadata {
  public ManifestUpdater getManifestUpdater();

  public Document getBlueprint();

  public void writeBlueprint( String name, OutputStream outputStream ) throws IOException;

  public FileWriter getFileWriter( String path ) throws IOException;
  OutputStream getFileOutputStream( String path ) throws IOException;

  void addContentType( String contentType );
  List<String> getContentTypes();

  void setBlueprint( Document blueprint );

  void executeAtEnd( Runnable runnable );
}
