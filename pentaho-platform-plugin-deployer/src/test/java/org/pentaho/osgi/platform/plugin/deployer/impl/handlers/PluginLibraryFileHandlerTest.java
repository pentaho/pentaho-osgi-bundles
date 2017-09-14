/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
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
