package org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.PluginXmlFileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS;

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
      if( !attributes.containsKey( "type" ) ){
        continue;
      }
      String contentType = attributes.get( "type" );
      pluginMetadata.addContentType( contentType );
    }
  }
}
