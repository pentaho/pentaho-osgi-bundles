/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.PluginXmlFileHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

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
    if( name == null ){
      // name is optional, some use title
      name = attributes.getNamedItem( "title" );
    }
    if( name != null ) {
      pluginMetadata.getManifestUpdater().setBundleSymbolicName( name.getNodeValue() );
    }
  }
}
