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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.ResourceBundle;

@Provider
@Produces( MediaType.APPLICATION_XML )
public class ResourceBundleXmlProvider implements MessageBodyWriter<ResourceBundle> {

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    boolean isXmlMediaType = MediaType.APPLICATION_XML_TYPE.equals( mediaType );

    return isXmlMediaType && ResourceBundle.class.isAssignableFrom( type );
  }

  @Override
  public long getSize( ResourceBundle resourceBundle, Class<?> type, Type genericType,
                       Annotation[] annotations, MediaType mediaType ) {
    return -1;
  }

  @Override
  public void writeTo( ResourceBundle resourceBundle, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
      throws IOException, WebApplicationException {

    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Node propertiesNode = document.createElement( "properties" );
      document.appendChild( propertiesNode );

      for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
        Node propertyNode = createPropertyElement( document, resourceBundle, key );
        propertiesNode.appendChild( propertyNode );
      }

      tranformData( document, entityStream );

    } catch ( ParserConfigurationException e ) {
      throw new WebApplicationException( e );
    }
  }

  private Node createPropertyElement( Document document, ResourceBundle resourceBundle, String key ) {
    Node propertyNode = document.createElement( "property" );

    Node keyNode = document.createElement( "key" );
    keyNode.setTextContent( key );
    propertyNode.appendChild( keyNode );

    Node valueNode = document.createElement( "value" );
    valueNode.setTextContent( resourceBundle.getString( key ) );
    propertyNode.appendChild( valueNode );

    return propertyNode;
  }

  private void tranformData( Document document, OutputStream entityStream ) throws IOException {
    Result output = new StreamResult( entityStream );
    Source input = new DOMSource( document );

    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      transformer.transform( input, output );

    } catch ( TransformerException e ) {
      throw new IOException( e );
    }
  }
}
