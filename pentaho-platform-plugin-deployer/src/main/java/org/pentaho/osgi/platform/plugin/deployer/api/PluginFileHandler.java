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
package org.pentaho.osgi.platform.plugin.deployer.api;

/**
 * Created by bryan on 8/26/14.
 */
public interface PluginFileHandler {
  public boolean handles( String fileName );

  public boolean handle( String relativePath, byte[] file, PluginMetadata pluginMetadata ) throws PluginHandlingException;
}
