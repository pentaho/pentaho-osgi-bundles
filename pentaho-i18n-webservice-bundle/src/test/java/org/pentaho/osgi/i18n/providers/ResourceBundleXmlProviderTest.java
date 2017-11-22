/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.osgi.i18n.providers;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ResourceBundleXmlProviderTest {
  private ResourceBundleXmlProvider xmlProvider;

  @Before
  public void setup() {
    this.xmlProvider = new ResourceBundleXmlProvider();
  }

  @Test
  public void testIsWritable() {
    MediaType xmlType = MediaType.APPLICATION_XML_TYPE;
    MediaType wildcardType = MediaType.WILDCARD_TYPE;

    assertTrue( this.xmlProvider.isWriteable( ResourceBundle.class, null, null, xmlType ) );
    assertFalse( this.xmlProvider.isWriteable( Object.class, null, null, xmlType ) );
    assertFalse( this.xmlProvider.isWriteable( ResourceBundle.class, null, null, wildcardType ) );
  }

  @Test
  public void testGetSize() {
    long expectedSize = -1;

    long size = this.xmlProvider.getSize( null, null, null, null, null );
    assertEquals( expectedSize, size );
  }

  @Test
  public void testXmlWrite() throws IOException, ParserConfigurationException, SAXException {
    Map<String, String> props = new HashMap<>();
    props.put( "key1", "value1" );
    props.put( "key2", "value2" );

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ResourceBundle resourceBundle = makeResourceBundle( props );
    MediaType xmlType = MediaType.APPLICATION_XML_TYPE;

    this.xmlProvider.writeTo( resourceBundle, ResourceBundle.class, null, null, xmlType,
        null, byteArrayOutputStream );

    Map<String, String> result = getWriteToResult( byteArrayOutputStream );
    int expectedSize = props.size();

    assertEquals( expectedSize, result.size() );
    assertEquals( props.get( "key1" ), result.get( "key1" ) );
    assertEquals( props.get( "key2" ), result.get( "key2" ) );
  }

  private ResourceBundle makeResourceBundle( final Map<String, String> map ) {
    return new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        Object[][] result = new Object[ map.size() ][];
        int index = 0;
        for ( Map.Entry<String, String> entry : map.entrySet() ) {
          Object[] entryObj = new Object[ 2 ];
          entryObj[ 0 ] = entry.getKey();
          entryObj[ 1 ] = entry.getValue();
          result[ index++ ] = entryObj;
        }
        return result;
      }
    };
  }

  private Map<String, String> documentToPropMap( Document document ) {
    Map<String, String> result = new HashMap<>();
    NodeList propertiesNodes = document.getElementsByTagName( "property" );
    for ( int i = 0; i < propertiesNodes.getLength(); i++ ) {
      NodeList propertyNodeList = propertiesNodes.item( i ).getChildNodes();
      String key = null;
      String value = null;
      for ( int o = 0; o < propertyNodeList.getLength(); o++ ) {
        Node node = propertyNodeList.item( o );
        if ( "key".equals( node.getNodeName() ) ) {
          key = node.getTextContent();
        } else if ( "value".equals( node.getNodeName() ) ) {
          value = node.getTextContent();
        }
      }
      result.put( key, value );
    }

    return result;
  }

  private Map<String, String> getWriteToResult( ByteArrayOutputStream out ) throws ParserConfigurationException, IOException, SAXException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse( new ByteArrayInputStream( out.toByteArray() ) );

    return documentToPropMap( doc );
  }
}
