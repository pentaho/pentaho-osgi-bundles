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
import org.pentaho.osgi.platform.plugin.deployer.impl.JSONUtil;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.PluginXmlFileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler
    .BLUEPRINT_BEAN_NS;

/**
 * Created by bryan on 8/29/14.
 */
public class PluginXmlExternalResourcesHandler extends PluginXmlFileHandler {
  public static final String EXTERNAL_RESOURCES_FILE = "META-INF/js/externalResources.json";

  private JSONUtil jsonUtil;

  public PluginXmlExternalResourcesHandler() {
    super( "//external-resources/file" );
  }

  public void setJsonUtil( JSONUtil jsonUtil ) {
    this.jsonUtil = jsonUtil;
  }

  @Override protected void handle( String relativePath, List<Node> nodes, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
    if ( nodes.size() > 0 ) {
      Map<String, List<String>> contextMap = new HashMap<String, List<String>>();
      for ( Node node : nodes ) {
        Map<String, String> attributes = getAttributes( node );
        String context = attributes.get( "context" );
        List<String> contextFiles = contextMap.get( context );
        if ( contextFiles == null ) {
          contextFiles = new ArrayList<String>();
          contextMap.put( context, contextFiles );
        }
        String contextFile = node.getTextContent();
        if ( !contextFile.startsWith( "/" ) ) {
          contextFile = "/" + contextFile;
        }
        contextFiles.add( contextFile );
      }

      // Pretty print the map while still escaping all variables
      FileWriter fileWriter = null;
      try {
        fileWriter = pluginMetadata.getFileWriter( EXTERNAL_RESOURCES_FILE );
        fileWriter.write( jsonUtil.prettyPrintMapStringListString( contextMap ) );
      } catch ( IOException e ) {
        throw new PluginHandlingException( e );
      } finally {
        if ( fileWriter != null ) {
          try {
            fileWriter.close();
          } catch ( IOException e ) {
            // Ignore
          }
        }
      }
      Document blueprint = pluginMetadata.getBlueprint();
      for ( Map.Entry<String, List<String>> stringListEntry : contextMap.entrySet() ) {
        for ( String string : stringListEntry.getValue() ) {
          Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
              "service" );
          service.setAttribute( "interface", "org.pentaho.platform.api.engine.IPlatformWebResource" );


          Element bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
              "bean" );
          bean.setAttribute( "class", "org.pentaho.platform.pdi.PlatformWebResource" );

          Element argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
          argument.setAttribute( "value", stringListEntry.getKey() );
          bean.appendChild( argument );

          argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
          argument.setAttribute( "value", string );
          bean.appendChild( argument );
          service.appendChild( bean );

          blueprint.getDocumentElement().appendChild( service );
        }
      }
    }
  }
}
