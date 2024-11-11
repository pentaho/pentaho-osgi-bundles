/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.osgi.i18n.webservice;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundle;
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

import static org.junit.Assert.*;

/**
 * Created by bryan on 9/6/14.
 */
public class ResourceBundleMessageBodyWriterTest {
  @Test
  public void testIsWritable() {
    ResourceBundleMessageBodyWriter resourceBundleMessageBodyWriter = new ResourceBundleMessageBodyWriter();
    assertTrue( resourceBundleMessageBodyWriter
      .isWriteable( OSGIResourceBundle.class, null, null, MediaType.APPLICATION_XML_TYPE ) );
    assertTrue( resourceBundleMessageBodyWriter
      .isWriteable( OSGIResourceBundle.class, null, null, MediaType.APPLICATION_JSON_TYPE ) );
    assertFalse(
      resourceBundleMessageBodyWriter.isWriteable( Object.class, null, null, MediaType.APPLICATION_XML_TYPE ) );
    assertFalse(
      resourceBundleMessageBodyWriter.isWriteable( OSGIResourceBundle.class, null, null, MediaType.WILDCARD_TYPE ) );
  }

  @Test
  public void testGetSize() {
    assertEquals( -1, new ResourceBundleMessageBodyWriter().getSize( null, null, null, null, null ) );
  }

  @Test
  public void testJsonWrite() throws IOException {
    Map<String, String> props = new HashMap<String, String>();
    props.put( "key1", "value1" );
    props.put( "key2", "value2" );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    new ResourceBundleMessageBodyWriter()
      .writeTo( makeResourceBundle( props ), ResourceBundle.class, null, null, MediaType.APPLICATION_JSON_TYPE, null,
        byteArrayOutputStream );
    JSONObject result = (JSONObject) JSONValue.parse( byteArrayOutputStream.toString() );
    assertEquals( 2, result.size() );
    assertEquals( "value1", result.get( "key1" ) );
    assertEquals( "value2", result.get( "key2" ) );
  }

  @Test
  public void testXmlWrite() throws IOException, ParserConfigurationException, SAXException {
    Map<String, String> props = new HashMap<String, String>();
    props.put( "key1", "value1" );
    props.put( "key2", "value2" );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    new ResourceBundleMessageBodyWriter()
      .writeTo( makeResourceBundle( props ), ResourceBundle.class, null, null, MediaType.APPLICATION_XML_TYPE, null,
        byteArrayOutputStream );
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      .parse( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );
    Map<String, String> result = documentToPropMap( doc );
    assertEquals( 2, result.size() );
    assertEquals( "value1", result.get( "key1" ) );
    assertEquals( "value2", result.get( "key2" ) );
  }

  public ResourceBundle makeResourceBundle( final Map<String, String> map ) {
    return new ListResourceBundle() {
      @Override protected Object[][] getContents() {
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

  public Map<String, String> documentToPropMap( Document document ) {
    Map<String, String> result = new HashMap<String, String>();
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
}
