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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bryan on 9/12/14.
 */

public class XMLListOutputBuilderTest {
  @Test
  public void testAdd() throws ParserConfigurationException, OutputException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    XMLListOutputBuilder xmlListOutputBuilder = new XMLListOutputBuilder( document, document );
    xmlListOutputBuilder.add( 2 );
    Node list = document.getFirstChild();
    assertEquals( XMLListOutputBuilder.LIST, ( (Element) list ).getTagName() );
    Node listEntry = list.getFirstChild();
    assertEquals( XMLListOutputBuilder.LIST_ENTRY, ( (Element) listEntry ).getTagName() );
    assertEquals( "2", listEntry.getTextContent() );
  }

  @Test
  public void testWrite() throws ParserConfigurationException, OutputException, IOException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    XMLListOutputBuilder xmlListOutputBuilder = new XMLListOutputBuilder( document, document );
    xmlListOutputBuilder.add( 2 );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    xmlListOutputBuilder.write( byteArrayOutputStream );
    assertTrue( byteArrayOutputStream.toString()
      .contains( "<" + XMLListOutputBuilder.LIST_ENTRY + ">2</" + XMLListOutputBuilder.LIST_ENTRY + ">" ) );
  }
}
