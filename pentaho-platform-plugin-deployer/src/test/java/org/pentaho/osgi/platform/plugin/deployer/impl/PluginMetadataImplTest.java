/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.osgi.platform.plugin.deployer.impl;

import com.google.common.io.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 8/27/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class PluginMetadataImplTest {

  @Mock File mockRootDirectory;
  @Mock Document blueprintDoc;

  @Test
  public void testConstructor() throws ParserConfigurationException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( new File( "." ) );
    assertNotNull( pluginMetadata.getBlueprint() );
    assertNotNull( pluginMetadata.getManifestUpdater() );
  }

  @Test
  public void testWriteBluePrint() throws ParserConfigurationException, IOException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( new File( "." ) );
    Document blueprint = pluginMetadata.getBlueprint();
    Node testNode = blueprint.createElementNS( "http://test.namespace/v1", "test" );
    blueprint.getElementsByTagNameNS( PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS, "blueprint" ).item( 0 )
      .appendChild( testNode );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    pluginMetadata.writeBlueprint( "test", byteArrayOutputStream );
    assertTrue( byteArrayOutputStream.toString( "UTF-8" ).contains( "<test xmlns=\"http://test.namespace/v1\"/>" ) );
  }

  @Test(expected = IOException.class)
  public void testWriteException() throws ParserConfigurationException, IOException {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( new File( "." ) );
    OutputStream outputStream = mock( OutputStream.class );
    doThrow( new IOException() ).when( outputStream ).write( any( byte[].class ), anyInt(), anyInt() );
    pluginMetadata.writeBlueprint( "test", outputStream );
  }

  @Test
  public void testGetFileWriter() throws Exception {
    File tmpDir = Files.createTempDir();
    String path = "my/test/sub/directory/file.txt";

    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( tmpDir );

    FileWriter fileWriter = pluginMetadata.getFileWriter( path );
    assertNotNull( fileWriter );

    fileWriter.write( "test" );
    fileWriter.flush();
    fileWriter.close();
    File expected = new File( tmpDir, path );
    assertTrue( expected.exists() );

    tmpDir.deleteOnExit();
  }

  @Test
  public void testGetFileOutputStream() throws Exception {
    File tmpDir = Files.createTempDir();
    String path = "my/test/sub/directory/file.txt";

    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( tmpDir );
    OutputStream fileOutputStream = pluginMetadata.getFileOutputStream( path );

    fileOutputStream.write( "test".getBytes() );
    fileOutputStream.flush();
    fileOutputStream.close();
    File expected = new File( tmpDir, path );
    assertTrue( expected.exists() );

    tmpDir.deleteOnExit();
  }

  @Test
  public void testSetBlueprint() throws Exception {

    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = document.createElement( "blueprint" );
    document.appendChild( root );
    root.appendChild( document.createElement( "bean" ) );
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( mockRootDirectory );
    pluginMetadata.setBlueprint( document );
    assertEquals( "bean", pluginMetadata.getBlueprint().getDocumentElement().getChildNodes().item( 0 ).getNodeName() );
  }

  @Test
  public void testAddContentType() throws Exception {
    PluginMetadataImpl pluginMetadata = new PluginMetadataImpl( mockRootDirectory );
    assertEquals( 0, pluginMetadata.getContentTypes().size() );

    pluginMetadata.addContentType( "text/xml" );
    assertEquals( 1, pluginMetadata.getContentTypes().size() );
    assertEquals( "text/xml", pluginMetadata.getContentTypes().get( 0 ) );

  }
}
