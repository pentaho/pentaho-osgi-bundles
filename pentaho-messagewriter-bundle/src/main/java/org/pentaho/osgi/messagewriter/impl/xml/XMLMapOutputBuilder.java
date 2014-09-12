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
import org.pentaho.osgi.messagewriter.builder.MapOutputBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by bryan on 9/11/14.
 */
public class XMLMapOutputBuilder extends XMLOutputBuilder implements MapOutputBuilder {
  public static final String MAP = "map";
  public static final String MAP_ENTRY = "mapEntry";
  public static final String KEY = "key";
  public static final String VALUE = "value";

  private final Node mapNode;

  public XMLMapOutputBuilder( Document document, Node parentNode ) {
    super( document );
    mapNode = document.createElement( MAP );
    parentNode.appendChild( mapNode );
  }

  @Override public void put( String key, Object value ) throws OutputException {
    Node entryNode = getDocument().createElement( MAP_ENTRY );
    mapNode.appendChild( entryNode );

    Node keyNode = getDocument().createElement( KEY );
    keyNode.setTextContent( key );
    entryNode.appendChild( keyNode );

    Node valueNode = getDocument().createElement( VALUE );
    XMLObjectOutputBuilder xmlObjectOutputBuilder = new XMLObjectOutputBuilder( getDocument(), valueNode );
    xmlObjectOutputBuilder.set( value );
    entryNode.appendChild( valueNode );
  }
}
