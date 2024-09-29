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

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.PluginXmlFileHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by nbaker on 8/22/16.
 */
public class PluginXmlPluginIdHandler  extends PluginXmlFileHandler {

  public PluginXmlPluginIdHandler(  ) {
    super( "/plugin" );
  }

  @Override protected void handle( String relativePath, List<Node> nodes, PluginMetadata pluginMetadata )
      throws PluginHandlingException {

    NamedNodeMap attributes = nodes.get( 0 ).getAttributes();
    Node name = attributes.getNamedItem( "name" );
    if ( name == null ) {
      // name is optional, some use title
      name = attributes.getNamedItem( "title" );
    }
    if ( name != null ) {
      pluginMetadata.getManifestUpdater().setBundleSymbolicName( name.getNodeValue() );
    }
  }
}
