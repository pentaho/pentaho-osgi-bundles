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

import org.pentaho.osgi.messagewriter.OutputException;
import org.pentaho.osgi.messagewriter.builder.ListOutputBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by bryan on 9/11/14.
 */
public class XMLListOutputBuilder extends XMLOutputBuilder implements ListOutputBuilder {
  public static final String LIST = "list";
  public static final String LIST_ENTRY = "listEntry";
  private final Node listNode;

  public XMLListOutputBuilder( Document document, Node parentNode ) {
    super( document );
    listNode = document.createElement( LIST );
    parentNode.appendChild( listNode );
  }

  @Override public void add( Object value ) throws OutputException {
    Node childNode = getDocument().createElement( LIST_ENTRY );
    listNode.appendChild( childNode );
    XMLObjectOutputBuilder xmlObjectOutputBuilder = new XMLObjectOutputBuilder( getDocument(), childNode );
    xmlObjectOutputBuilder.set( value );
  }
}
