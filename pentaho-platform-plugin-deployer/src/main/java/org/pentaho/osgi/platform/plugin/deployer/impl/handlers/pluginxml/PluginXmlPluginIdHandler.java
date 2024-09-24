/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
