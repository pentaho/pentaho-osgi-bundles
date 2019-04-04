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
import org.w3c.dom.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bryan on 8/26/14.
 */
public class PluginXmlStaticPathsHandler extends PluginXmlFileHandler {
  public static final String STATIC_RESOURCES_FILE = "META-INF/js/staticResources.json";
  public static final String BLUEPRINT_BEAN_NS = "http://www.osgi.org/xmlns/blueprint/v1.0.0";
  public static final String BEAN = "bean";
  public static final String PROPERTY = "property";
  public static final String SERVICE = "service";

  public static final String ID_ATTR = "id";
  public static final String REF_ATTR = "ref";
  public static final String INTERFACE_ATTR = "interface";
  public static final String CLASS_ATTR = "class";
  public static final String NAME_ATTR = "name";
  public static final String VALUE_ATTR = "value";

  public static final String ALIAS = "alias";
  public static final String PATH = "path";

  public static final String RESOURCE_MAPPING = "org.ops4j.pax.web.service.whiteboard.ResourceMapping";
  public static final String DEFAULT_RESOURCE_MAPPING =
    "org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping";

  private JSONUtil jsonUtil;

  public PluginXmlStaticPathsHandler() {
    super( "//static-path" );
  }

  public void setJsonUtil( JSONUtil jsonUtil ) {
    this.jsonUtil = jsonUtil;
  }

  @Override protected void handle( String relativePath, List<Node> nodes, PluginMetadata pluginMetadata )
    throws PluginHandlingException {
    String topLevelFolder = relativePath.split( "/" )[ 0 ];
    Document blueprint = pluginMetadata.getBlueprint();
    boolean foundResources = false;
    Map<String, String> urlToResourceMapping = new HashMap<String, String>();
    for ( Node node : nodes ) {
      Map<String, String> attributes = getAttributes( node );
      final String url = attributes.get( "url" );
      String localFolder = attributes.get( "localFolder" );
      if ( url != null && localFolder != null ) {
        String contentUrl = "/content" + url;
        List<String> urls = new ArrayList<String>( );
        urls.add( contentUrl );
        for ( String u : urls ) {
          foundResources = true;
          Node bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS, BEAN );

          Node aliasProperty = blueprint.createElementNS( BLUEPRINT_BEAN_NS, PROPERTY );
          Node pathProperty = blueprint.createElementNS( BLUEPRINT_BEAN_NS, PROPERTY );
          Node service = blueprint.createElementNS( BLUEPRINT_BEAN_NS, SERVICE );

          blueprint.getDocumentElement().appendChild( bean );
          bean.appendChild( aliasProperty );
          bean.appendChild( pathProperty );
          blueprint.getDocumentElement().appendChild( service );

          String id = getResourceMappingId( u, localFolder );
          setAttribute( blueprint, bean, ID_ATTR, id );
          setAttribute( blueprint, bean, CLASS_ATTR, DEFAULT_RESOURCE_MAPPING );
          setAttribute( blueprint, aliasProperty, NAME_ATTR, ALIAS );
          setAttribute( blueprint, aliasProperty, VALUE_ATTR, u  );
          setAttribute( blueprint, pathProperty, NAME_ATTR, PATH );
          String path = "/" + topLevelFolder + "/" + localFolder;
          setAttribute( blueprint, pathProperty, VALUE_ATTR, path );
          setAttribute( blueprint, service, ID_ATTR, id + "Service" );
          setAttribute( blueprint, service, REF_ATTR, id );
          setAttribute( blueprint, service, INTERFACE_ATTR, RESOURCE_MAPPING );

          urlToResourceMapping.put( u, path );
        }
      }
    }
    if ( foundResources ) {
      Map<String, String> imports = pluginMetadata.getManifestUpdater().getImports();
      imports.put( "org.ops4j.pax.web.service.whiteboard", null );
      imports.put( "org.ops4j.pax.web.extender.whiteboard.runtime", null );
      imports.put( "org.osgi.service.blueprint", "[1.0.0,2.0.0)" );
      pluginMetadata.getManifestUpdater().getExportServices()
        .add( "org.ops4j.pax.web.service.whiteboard.ResourceMapping" );

      FileWriter fileWriter = null;
      try {
        fileWriter = pluginMetadata.getFileWriter( STATIC_RESOURCES_FILE );
        fileWriter.write( jsonUtil.prettyPrintMapStringString( urlToResourceMapping ) );
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
    }
  }

  protected String getResourceMappingId( String url, String localFile ) {
    StringBuilder sb = new StringBuilder();
    sb.append( camelCaseJoin( url ) );
    sb.append( "MappingTo" );
    sb.append( camelCaseJoin( localFile ) );
    return sb.toString();
  }
}
