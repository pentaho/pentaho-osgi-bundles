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
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by nbaker on 7/31/16.
 */
public class PluginXmlContentTypeHandler extends PluginXmlFileHandler {

  public PluginXmlContentTypeHandler(  ) {
    super( "//content-types/content-type" );
  }

  @Override protected void handle( String relativePath, List<Node> nodes, PluginMetadata pluginMetadata )
      throws PluginHandlingException {

    for ( Node node : nodes ) {
      Map<String, String> attributes = getAttributes( node );
      if ( !attributes.containsKey( "type" ) ) {
        continue;
      }
      String contentType = attributes.get( "type" );
      pluginMetadata.addContentType( contentType );
    }
  }
}
