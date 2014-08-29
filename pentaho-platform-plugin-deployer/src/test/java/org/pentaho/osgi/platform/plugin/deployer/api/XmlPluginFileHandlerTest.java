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

package org.pentaho.osgi.platform.plugin.deployer.api;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/27/14.
 */
public class XmlPluginFileHandlerTest {

  @Test( expected = PluginHandlingException.class )
  public void testHandleThrowsPluginHandlingException() throws UnsupportedEncodingException, PluginHandlingException {
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler() {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {
        throw new NullPointerException();
      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    xmlPluginFileHandler.handle( "test-filename/plugin.xml", "<plugin></plugin>".getBytes( "UTF-8" ), null );
  }

  @Test
  public void testHandleDelegates() throws UnsupportedEncodingException, PluginHandlingException {
    final String testFilename = "test-filename/plugin.xml";
    String xml = "<plugin></plugin>";
    final PluginMetadata pluginMetadataMock = mock( PluginMetadata.class );
    final AtomicBoolean criteriaPassed = new AtomicBoolean( false );
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler( "test" ) {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {
        if ( pluginMetadata == pluginMetadataMock && testFilename.equals( fileName ) && nodes.size() == 0 ) {
          criteriaPassed.set( true );
        }
      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    xmlPluginFileHandler.handle( "test-filename/plugin.xml", xml.getBytes( "UTF-8" ), pluginMetadataMock );
    assertTrue( criteriaPassed.get() );
  }

  private NodeList mockNodeList( final Node... childNodes ) {
    NodeList result = mock( NodeList.class );
    when( result.getLength() ).thenReturn( childNodes.length );
    when( result.item( anyInt() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return childNodes[ (Integer) invocation.getArguments()[ 0 ] ];
      }
    } );
    return result;
  }

  @Test
  public void testGetNodes() {
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler( "test", "ing", "path" ) {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {

      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    Document document = mock( Document.class );

    Node testNode = mock( Node.class );
    Node ingNode = mock( Node.class );
    Node pathNode = mock( Node.class );

    when( testNode.getLocalName() ).thenReturn( "test" );
    when( ingNode.getLocalName() ).thenReturn( "ing" );
    when( pathNode.getLocalName() ).thenReturn( "path" );

    NodeList testNodeList = mockNodeList( testNode, ingNode, pathNode );
    when( document.getChildNodes() ).thenReturn( testNodeList );
    NodeList ingNodeList = mockNodeList( ingNode, pathNode );
    when( testNode.getChildNodes() ).thenReturn( ingNodeList );
    NodeList pathNodeList = mockNodeList( pathNode );
    when( ingNode.getChildNodes() ).thenReturn( pathNodeList );

    List<Node> nodes = new ArrayList<Node>( Arrays.asList( pathNode ) );
    assertEquals( nodes, xmlPluginFileHandler.getNodes( document, 0 ) );
  }

  @Test
  public void testGetAttributes() {
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler() {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {

      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    Node node = mock( Node.class );
    String attribute = "TEST_ATTR";
    String attribute2 = "TEST_ATTR2";
    String value = "TEST_VALUE";
    String value2 = "TEST_VALUE2";
    Attr attr = mock( Attr.class );
    Attr attr2 = mock( Attr.class );
    NamedNodeMap namedNodeMap = mock( NamedNodeMap.class );
    when( namedNodeMap.getLength() ).thenReturn( 2 );
    when( namedNodeMap.item( 0 ) ).thenReturn( attr );
    when( namedNodeMap.item( 1 ) ).thenReturn( attr2 );
    when( attr.getName() ).thenReturn( attribute );
    when( attr.getValue() ).thenReturn( value );
    when( attr2.getName() ).thenReturn( attribute2 );
    when( attr2.getValue() ).thenReturn( value2 );
    when( node.getAttributes() ).thenReturn( namedNodeMap );
    Map<String, String> attributes = xmlPluginFileHandler.getAttributes( node );
    assertEquals( 2, attributes.size() );
    assertEquals( value, attributes.get( attribute ) );
    assertEquals( value2, attributes.get( attribute2 ) );
  }

  @Test
  public void testSetAttribute() {
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler() {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {

      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    Document document = mock( Document.class );
    Node node = mock( Node.class );
    String attribute = "TEST_ATTR";
    String value = "TEST_VALUE";
    Attr attr = mock( Attr.class );
    NamedNodeMap namedNodeMap = mock( NamedNodeMap.class );
    when( node.getAttributes() ).thenReturn( namedNodeMap );
    when( document.createAttribute( attribute ) ).thenReturn( attr );
    xmlPluginFileHandler.setAttribute( document, node, attribute, value );
    verify( attr ).setValue( value );
    verify( namedNodeMap ).setNamedItem( attr );
  }

  @Test
  public void testCamelCaseJoinRemovesSpecialCharactersAndCamelCases() {
    XmlPluginFileHandler xmlPluginFileHandler = new XmlPluginFileHandler() {
      @Override protected void handle( String fileName, List<Node> nodes, PluginMetadata pluginMetadata )
        throws PluginHandlingException {

      }

      @Override public boolean handles( String fileName ) {
        return false;
      }
    };
    assertEquals( "camelCaseC", xmlPluginFileHandler.camelCaseJoin( "camel-+!case/c" ) );
  }
}
