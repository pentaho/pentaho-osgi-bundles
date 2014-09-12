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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 9/12/14.
 */
public class XMLMapOutputBuilderTest {
  @Test
  public void testPut() throws ParserConfigurationException, OutputException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    XMLMapOutputBuilder xmlMapOutputBuilder = new XMLMapOutputBuilder( document, document );
    xmlMapOutputBuilder.put( "two", 2 );
    Node map = document.getFirstChild();
    assertEquals( XMLMapOutputBuilder.MAP, ( (Element) map ).getTagName() );
    Node mapEntry = map.getFirstChild();
    assertEquals( XMLMapOutputBuilder.MAP_ENTRY, ( (Element) mapEntry ).getTagName() );
    Node keyNode = mapEntry.getFirstChild();
    assertEquals( XMLMapOutputBuilder.KEY, ( (Element) keyNode ).getTagName() );
    assertEquals( "two", keyNode.getTextContent() );
    Node valueNode = keyNode.getNextSibling();
    assertEquals( XMLMapOutputBuilder.VALUE, ( (Element) valueNode ).getTagName() );
    assertEquals( "2", valueNode.getTextContent() );
  }
}
