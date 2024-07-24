/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import org.apache.xerces.dom.ElementNSImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.platform.plugin.deployer.api.ManifestUpdater;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.impl.JSONUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/26/14.
 */
public class PluginXmlStaticPathsHandlerTest {
  public static final String RESOURCE_PATTERN_KEY = "osgi.http.whiteboard.resource.pattern";
  public static final String RESOURCE_PREFIX_KEY = "osgi.http.whiteboard.resource.prefix";

  private JSONUtil jsonUtil;

  @Before
  public void setup() {
    jsonUtil = mock( JSONUtil.class );
  }

  @Test
  public void testHandlesNull() {
    assertFalse( new PluginXmlStaticPathsHandler().handles( null ) );
  }

  @Test
  public void testHandlesFalse() {
    assertFalse( new PluginXmlStaticPathsHandler().handles( "plugin.xml" ) );
    assertFalse( new PluginXmlStaticPathsHandler().handles( "testme/pleaze.xml" ) );
  }

  @Test
  public void testHandlesTrue() {
    assertTrue( new PluginXmlStaticPathsHandler().handles( "testme/plugin.xml" ) );
  }

  @Test
  public void testResourceMapping()
    throws ParserConfigurationException, PluginHandlingException, TransformerException, IOException {
    Map<String, String> mockNode1Values = new HashMap<String, String>();
    mockNode1Values.put( "url", "/common-ui/resources" );
    mockNode1Values.put( "localFolder", "resources" );
    Map<String, String> mockNode2Values = new HashMap<String, String>();
    Map<String, String> mockNode3Values = new HashMap<String, String>();
    mockNode3Values.put( "localFolder", "resources" );
    Map<String, String> mockNode4Values = new HashMap<String, String>();
    mockNode4Values.put( "url", "/common-ui/resources" );
    List<Node> nodes =
      new ArrayList<Node>( Arrays
        .asList( makeMockNode( mockNode1Values ), makeMockNode( mockNode2Values ), makeMockNode( mockNode3Values ),
          makeMockNode( mockNode4Values ) ) );
    PluginMetadata pluginMetadata = mock( PluginMetadata.class );
    FileWriter fileWriter = mock( FileWriter.class );
    when( pluginMetadata.getFileWriter( anyString() ) ).thenReturn( fileWriter );
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Node blueprintNode = document.createElementNS( PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS, "blueprint" );
    document.appendChild( blueprintNode );
    when( pluginMetadata.getBlueprint() ).thenReturn( document );
    ManifestUpdater manifestUpdater = mock( ManifestUpdater.class );
    when( pluginMetadata.getManifestUpdater() ).thenReturn( manifestUpdater );
    PluginXmlStaticPathsHandler pluginXmlStaticPathsHandler = new PluginXmlStaticPathsHandler();
    pluginXmlStaticPathsHandler.setJsonUtil( new JSONUtil() );
    pluginXmlStaticPathsHandler.handle( "test-plugin/plugin.xml", nodes, pluginMetadata );
    NodeList nodeList = document.getElementsByTagNameNS( PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS, "service" );
    assertEquals( 1, nodeList.getLength() );
    Node node = ( (ElementNSImpl) nodeList.item( 0 ) ).getElementsByTagName( "service-properties" ).item( 0 );
    NodeList propertyNodes = node.getChildNodes();
    assertEquals( 2, propertyNodes.getLength() );
    Node aliasNode = propertyNodes.item( 0 );
    Node pathNode = propertyNodes.item( 1 );
    assertEquals( RESOURCE_PATTERN_KEY, ( (Attr) aliasNode.getAttributes().getNamedItem( "key" ) ).getValue() );
    assertEquals( "/content/common-ui/resources/*", ( (Attr) aliasNode.getAttributes().getNamedItem( "value" ) ).getValue() );
    assertEquals( RESOURCE_PREFIX_KEY, ( (Attr) pathNode.getAttributes().getNamedItem( "key" ) ).getValue() );
    assertEquals( "/test-plugin/resources", ( (Attr) pathNode.getAttributes().getNamedItem( "value" ) ).getValue() );
  }

  @Test
  public void testResourceMappingNoneFound()
    throws ParserConfigurationException, PluginHandlingException, TransformerException {
    Map<String, String> mockNode2Values = new HashMap<String, String>();
    Map<String, String> mockNode3Values = new HashMap<String, String>();
    mockNode3Values.put( "localFolder", "resources" );
    Map<String, String> mockNode4Values = new HashMap<String, String>();
    mockNode4Values.put( "url", "/common-ui/resources" );
    List<Node> nodes =
      new ArrayList<Node>( Arrays
        .asList( makeMockNode( mockNode2Values ), makeMockNode( mockNode3Values ),
          makeMockNode( mockNode4Values ) ) );
    PluginMetadata pluginMetadata = mock( PluginMetadata.class );
    PluginXmlStaticPathsHandler pluginXmlStaticPathsHandler = new PluginXmlStaticPathsHandler();
    pluginXmlStaticPathsHandler.setJsonUtil( jsonUtil );
    pluginXmlStaticPathsHandler.handle( "test-plugin/plugin.xml", nodes, pluginMetadata );
    verify( pluginMetadata ).getBlueprint();
    verifyNoMoreInteractions( pluginMetadata );
    verifyNoMoreInteractions( jsonUtil );
  }

  public static Node makeMockNode( final Map<String, String> attributes ) {
    NamedNodeMap namedNodeMap = mock( NamedNodeMap.class );
    final List<String> keys = new ArrayList<String>( attributes.keySet() );
    when( namedNodeMap.getLength() ).thenReturn( attributes.size() );
    when( namedNodeMap.item( anyInt() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        Attr attr = mock( Attr.class );
        when( attr.getName() ).thenReturn( keys.get( (Integer) invocation.getArguments()[ 0 ] ) );
        when( attr.getValue() ).thenReturn( attributes.get( keys.get( (Integer) invocation.getArguments()[ 0 ] ) ) );
        return attr;
      }
    } );
    Node node = mock( Node.class );
    when( node.getAttributes() ).thenReturn( namedNodeMap );
    return node;
  }
}
