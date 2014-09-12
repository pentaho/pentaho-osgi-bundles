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

import org.junit.Test;
import org.pentaho.osgi.messagewriter.OutputException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by bryan on 9/12/14.
 */
public class XMLPrimitiveOutputBuilderTest {
  @Test
  public void testSet() throws ParserConfigurationException, OutputException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Node parentNode = document.createElement( "node" );
    document.appendChild( parentNode );
    XMLPrimitiveOutputBuilder xmlPrimitiveOutputBuilder = new XMLPrimitiveOutputBuilder( document, parentNode );
    xmlPrimitiveOutputBuilder.set( 2 );
    assertEquals( "2", parentNode.getTextContent() );
  }

  @Test
  public void testNull() throws ParserConfigurationException, OutputException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Node parentNode = mock( Node.class );
    XMLPrimitiveOutputBuilder xmlPrimitiveOutputBuilder = new XMLPrimitiveOutputBuilder( document, parentNode );
    xmlPrimitiveOutputBuilder.set( null );
    verifyNoMoreInteractions( parentNode );
  }
}
