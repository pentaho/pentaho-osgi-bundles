/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.api.XmlPluginFileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by bryan on 8/26/14.
 */
public class PluginXmlStaticPathsHandler extends XmlPluginFileHandler {
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

  public static final String RESOURCE_MAPPING = "org.ops4j.pax.web.extender.whiteboard.ResourceMapping";
  public static final String DEFAULT_RESOURCE_MAPPING =
    "org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping";

  public PluginXmlStaticPathsHandler() {
    super( "plugin", "static-paths", "static-path" );
  }

  @Override public boolean handles( String fileName ) {
    if ( fileName != null ) {
      String[] splitName = fileName.split( "/" );
      if ( splitName.length == 2 && "plugin.xml".equals( splitName[ 1 ] ) ) {
        return true;
      }
    }
    return false;
  }

  @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata ) throws PluginHandlingException {
    String topLevelFolder = fileName.split( "/" )[0];
    Document blueprint = pluginMetadata.getBlueprint();
    boolean foundResources = false;
    for ( Node node : nodes ) {
      Map<String, String> attributes = getAttributes( node );
      String url = attributes.get( "url" );
      String localFolder = attributes.get( "localFolder" );
      if ( url != null && localFolder != null ) {
        foundResources = true;
        Node bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS, BEAN );

        Node aliasProperty = blueprint.createElementNS( BLUEPRINT_BEAN_NS, PROPERTY );
        Node pathProperty = blueprint.createElementNS( BLUEPRINT_BEAN_NS, PROPERTY );
        Node service = blueprint.createElementNS( BLUEPRINT_BEAN_NS, SERVICE );

        blueprint.getDocumentElement().appendChild( bean );
        bean.appendChild( aliasProperty );
        bean.appendChild( pathProperty );
        blueprint.getDocumentElement().appendChild( service );

        String id = getResourceMappingId( url, localFolder );
        setAttribute( blueprint, bean, ID_ATTR, id );
        setAttribute( blueprint, bean, CLASS_ATTR, DEFAULT_RESOURCE_MAPPING );
        setAttribute( blueprint, aliasProperty, NAME_ATTR, ALIAS );
        setAttribute( blueprint, aliasProperty, VALUE_ATTR, url );
        setAttribute( blueprint, pathProperty, NAME_ATTR, PATH );
        setAttribute( blueprint, pathProperty, VALUE_ATTR, "/" + topLevelFolder + "/" + localFolder );
        setAttribute( blueprint, service, ID_ATTR, id + "Service" );
        setAttribute( blueprint, service, REF_ATTR, id );
        setAttribute( blueprint, service, INTERFACE_ATTR, RESOURCE_MAPPING );
      }
    }
    if ( foundResources ) {
      Map<String, String> imports = pluginMetadata.getManifestUpdater().getImports();
      imports.put( "org.ops4j.pax.web.extender.whiteboard", null );
      imports.put( "org.ops4j.pax.web.extender.whiteboard.runtime", null );
      imports.put( "org.osgi.service.blueprint", "[1.0.0,2.0.0)" );
      pluginMetadata.getManifestUpdater().getExportServices().add( "org.ops4j.pax.web.extender.whiteboard.ResourceMapping" );
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
