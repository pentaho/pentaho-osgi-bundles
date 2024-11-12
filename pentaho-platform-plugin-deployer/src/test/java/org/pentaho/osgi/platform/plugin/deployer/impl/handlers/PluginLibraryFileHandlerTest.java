/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 8/29/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class PluginLibraryFileHandlerTest {
  PluginLibraryFileHandler pluginLibraryFileHandler;

  File testResources;

  @Mock PluginMetadata pluginMetadata;
  @Mock OutputStream outputStream;
  @Mock FileWriter fileWriter;

  @Before
  public void setUp() throws Exception {
    pluginLibraryFileHandler = new PluginLibraryFileHandler();
    testResources = new File( "src/test/resources" );
  }

  @Test
  public void testHandles() throws Exception {
    assertFalse( pluginLibraryFileHandler.handles( "test.jar" ) );
    assertFalse( pluginLibraryFileHandler.handles( "root/test.jar" ) );

    assertTrue( pluginLibraryFileHandler.handles( "root/lib/test.jar" ) );
    assertTrue( pluginLibraryFileHandler.handles( "root/branch/lib/test.jar" ) );
  }

  @Test
  public void testHandle_shouldSkipProcessingOfOsgiBundleJar() throws Exception {
    File f = new File( testResources,
      "org/pentaho/osgi/platform/plugin/deployer/impl/handlers/test-dummy-bundle.jar" );

    pluginLibraryFileHandler.handle( "", IOUtils.toByteArray( new FileInputStream( f ) ), pluginMetadata );

    verify( pluginMetadata, never() ).getFileOutputStream( anyString() );
    verify( pluginMetadata, never() ).getFileWriter( anyString() );
  }

  @Test
  public void testHandle() throws Exception {
    File f = new File( testResources,
      "org/pentaho/osgi/platform/plugin/deployer/impl/handlers/test-dummy.jar" );

    when( pluginMetadata.getFileOutputStream( anyString() ) ).thenReturn( outputStream );
    when( pluginMetadata.getFileWriter( anyString() ) ).thenReturn( fileWriter );

    pluginLibraryFileHandler.handle( "", IOUtils.toByteArray( new FileInputStream( f ) ), pluginMetadata );

    verify( pluginMetadata ).getFileOutputStream( "org/pentaho/App.class" );
    verify( pluginMetadata ).getFileWriter( "META-INF/spring/beans.xml" );

    verify( outputStream, times( 2 ) ).write( any() );
    verify( outputStream, times( 2 ) ).close();

    verify( fileWriter ).append( anyString() );
    verify( fileWriter ).close();

  }

}