/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.XmlPluginFileHandler;

/**
 * Created by bryan on 8/29/14.
 */
public abstract class PluginXmlFileHandler extends XmlPluginFileHandler {

  public static final String PLUGIN_XML_FILENAME = "plugin.xml";

  public PluginXmlFileHandler( String xpath ) {
    super( xpath );
  }
  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.endsWith( "/" + PLUGIN_XML_FILENAME );
  }
}
