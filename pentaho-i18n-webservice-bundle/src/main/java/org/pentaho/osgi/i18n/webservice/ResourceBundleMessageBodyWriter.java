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
package org.pentaho.osgi.i18n.webservice;

import org.json.simple.JSONObject;
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
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/5/14.
 */
@Provider
@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
public class ResourceBundleMessageBodyWriter implements MessageBodyWriter<ResourceBundle> {

  @Override public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations,
                                        MediaType mediaType ) {
    return ResourceBundle.class.isAssignableFrom( type ) && ( MediaType.APPLICATION_JSON_TYPE.equals( mediaType )
      || MediaType.APPLICATION_XML_TYPE.equals( mediaType ) );
  }

  @Override public long getSize( ResourceBundle resourceBundle, Class<?> type, Type genericType,
                                 Annotation[] annotations, MediaType mediaType ) {
    return -1;
  }

  @Override public void writeTo( ResourceBundle resourceBundle, Class<?> type, Type genericType,
                                 Annotation[] annotations, MediaType mediaType,
                                 MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
    throws IOException, WebApplicationException {
    if ( MediaType.APPLICATION_JSON_TYPE.equals( mediaType ) ) {
      JSONObject resourceBundleJsonObject = new JSONObject();
      for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
        resourceBundleJsonObject.put( key, resourceBundle.getString( key ) );
      }
      OutputStreamWriter outputStreamWriter = null;
      try {
        outputStreamWriter = new OutputStreamWriter( entityStream, StandardCharsets.UTF_8 );
        resourceBundleJsonObject.writeJSONString( outputStreamWriter );
      } finally {
        if ( outputStreamWriter != null ) {
          outputStreamWriter.flush();
        }
      }
    } else if ( MediaType.APPLICATION_XML_TYPE.equals( mediaType ) ) {
      try {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node propertiesNode = document.createElement( "properties" );
        document.appendChild( propertiesNode );
        for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
          Node propertyNode = document.createElement( "property" );
          propertiesNode.appendChild( propertyNode );

          Node keyNode = document.createElement( "key" );
          keyNode.setTextContent( key );
          propertyNode.appendChild( keyNode );

          Node valueNode = document.createElement( "value" );
          valueNode.setTextContent( resourceBundle.getString( key ) );
          propertyNode.appendChild( valueNode );
        }
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
      } catch ( ParserConfigurationException e ) {
        throw new WebApplicationException( e );
      }
    }
  }
}
