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

package org.pentaho.osgi.messagewriter.impl.xml;

import org.pentaho.osgi.messagewriter.PrimitiveOutputBuilder;
import org.pentaho.osgi.messagewriter.builder.ListOutputBuilder;
import org.pentaho.osgi.messagewriter.builder.MapOutputBuilder;
import org.pentaho.osgi.messagewriter.builder.OutputBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by bryan on 9/11/14.
 */
public class XMLOutputBuilderFactory implements OutputBuilderFactory {
  private static final Logger log = LoggerFactory.getLogger( XMLOutputBuilderFactory.class );
  private final Document document;
  private final Node parentNode;

  public XMLOutputBuilderFactory() {
    this( null, null );
  }

  public XMLOutputBuilderFactory( Document document, Node parentNode ) {
    this.document = document;
    if ( parentNode == null ) {
      this.parentNode = document;
    } else {
      this.parentNode = parentNode;
    }
  }

  public Document getDocument() {
    Document document = this.document;
    if ( document == null ) {
      try {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      } catch ( ParserConfigurationException e ) {
        log.error( e.getMessage(), e );
      }
    }
    return document;
  }

  @Override public String mediaType() {
    return "application";
  }

  @Override public String subType() {
    return "xml";
  }

  @Override public MapOutputBuilder createMapOutputBuilder() {
    Document document = getDocument();
    Node parentNode = this.parentNode;
    if ( parentNode == null ) {
      parentNode = document;
    }
    return new XMLMapOutputBuilder( document, parentNode );
  }

  @Override public ListOutputBuilder createListOutputBuilder() {
    Document document = getDocument();
    Node parentNode = this.parentNode;
    if ( parentNode == null ) {
      parentNode = document;
    }
    return new XMLListOutputBuilder( document, parentNode );
  }

  @Override public PrimitiveOutputBuilder createPrimitiveOutputBuilder() {
    Document document = getDocument();
    Node parentNode = this.parentNode;
    if ( parentNode == null ) {
      parentNode = document;
    }
    return new XMLPrimitiveOutputBuilder( document, parentNode );
  }
}
