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
