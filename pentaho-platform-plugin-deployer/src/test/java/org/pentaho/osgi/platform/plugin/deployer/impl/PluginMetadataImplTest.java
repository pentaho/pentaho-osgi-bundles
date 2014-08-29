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

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/27/14.
 */
public class PluginMetadataImplTest {
  @Test
  public void testConstructor() throws ParserConfigurationException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl();
    assertNotNull( pluginMetadata.getBlueprint() );
    assertNotNull( pluginMetadata.getManifestUpdater() );
  }

  @Test
  public void testWriteBluePrint() throws ParserConfigurationException, IOException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl();
    Document blueprint = pluginMetadata.getBlueprint();
    Node testNode = blueprint.createElementNS( "http://test.namespace/v1", "test" );
    blueprint.getElementsByTagNameNS( PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS, "blueprint" ).item( 0 )
      .appendChild( testNode );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    pluginMetadata.writeBlueprint( byteArrayOutputStream );
    assertTrue( byteArrayOutputStream.toString( "UTF-8" ).contains( "<test xmlns=\"http://test.namespace/v1\"/>" ) );
  }

  @Test( expected = IOException.class )
  public void testWriteException() throws ParserConfigurationException, IOException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl();
    OutputStream outputStream = mock( OutputStream.class );
    doThrow( new IOException( ) ).when( outputStream ).write( any( byte[].class ) );
    doThrow( new IOException( ) ).when( outputStream ).write( any( byte.class ) );
    doThrow( new IOException( ) ).when( outputStream ).write( any( byte[].class ), anyInt(), anyInt() );
    pluginMetadata.writeBlueprint( outputStream );
  }
}
