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

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/28/14.
 */
public class PluginZipFileProcessorTest {
  private ExecutorService executorService;

  @Before
  public void setup() {
    executorService = mock( ExecutorService.class );
    when( executorService.submit( any( Callable.class ) ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        Object result = ( (Callable) invocation.getArguments()[ 0 ] ).call();
        Future future = mock( Future.class );
        when( future.get() ).thenReturn( result );
        return future;
      }
    } );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testProcessBackgroundWithException() throws IOException {
    List<PluginFileHandler> pluginFileHandlers = new ArrayList<PluginFileHandler>();
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, false, "test", "test-symbolic", "version" );
    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    ZipInputStream zipInputStream = mock( ZipInputStream.class );
    ExceptionSettable<Throwable> exceptionSettable = mock( ExceptionSettable.class );
    IOException myException = new IOException( "TEST_EXCEPTION" );
    when( zipInputStream.getNextEntry() ).thenThrow( myException );
    pluginZipFileProcessor.processBackground( executorService, () -> zipInputStream, zipOutputStream, exceptionSettable );
    verify( exceptionSettable ).setException( myException );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testProcessBackgroundWithNoException() throws IOException {
    List<PluginFileHandler> pluginFileHandlers = new ArrayList<PluginFileHandler>();
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, false, "test", "test-symbolic", "version" );
    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    ZipInputStream zipInputStream = mock( ZipInputStream.class );
    ExceptionSettable<Throwable> exceptionSettable = mock( ExceptionSettable.class );
    pluginZipFileProcessor.processBackground( executorService, () -> zipInputStream, zipOutputStream, exceptionSettable );
    verifyNoMoreInteractions( exceptionSettable );
  }

  @Test
  public void testProcess() throws IOException {
    List<PluginFileHandler> pluginFileHandlers = new ArrayList<PluginFileHandler>();
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, false, "test", "test-symbolic", "version" );
    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    pluginZipFileProcessor.process( () -> new ZipInputStream( this.getClass().getClassLoader()
        .getResourceAsStream( "org/pentaho/osgi/platform/plugin/deployer/testCanHandleWithPluginXmlOneDirDown.zip" ) ),
      zipOutputStream );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "test-plugin/" ) ) ) );
    verify( zipOutputStream )
      .putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "test-plugin/plugin.xml" ) ) ) );
    verify( zipOutputStream )
      .putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "test-plugin/other-file" ) ) ) );
    verify( zipOutputStream ).putNextEntry(
      argThat( new ZipEntryMatcher( new ZipEntry( PluginZipFileProcessor.BLUEPRINT ) ) ) );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( JarFile.MANIFEST_NAME ) ) ) );
  }

  @Test
  public void testProcessCloseExceptions() throws IOException {
    List<PluginFileHandler> pluginFileHandlers = new ArrayList<PluginFileHandler>();
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, false, "test", "test-symbolic", "version" );
    ZipInputStream zipInputStream = mock( ZipInputStream.class );
    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    pluginZipFileProcessor.process( () -> zipInputStream, zipOutputStream );
    doThrow( new IOException() ).when( zipInputStream ).close();
    doThrow( new IOException() ).when( zipOutputStream ).close();
  }

  @Test
  public void testProcessWithManifestAndBlueprint() throws IOException {
    List<PluginFileHandler> pluginFileHandlers =
      new ArrayList<PluginFileHandler>( Arrays.asList( new PluginXmlStaticPathsHandler() ) );
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, false, "test", "test-symbolic", "version" );
    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    pluginZipFileProcessor.process( () -> new ZipInputStream( this.getClass().getClassLoader()
        .getResourceAsStream( "org/pentaho/osgi/platform/plugin/deployer/testWithManifestAndBlueprint.zip" ) ),
      zipOutputStream );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "META-INF/" ) ) ) );
    verify( zipOutputStream, times( 1 ) )
      .putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( JarFile.MANIFEST_NAME ) ) ) );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "OSGI-INF/" ) ) ) );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "OSGI-INF/blueprint/" ) ) ) );
    verify( zipOutputStream, times( 1 ) ).putNextEntry(
      argThat( new ZipEntryMatcher( new ZipEntry( PluginZipFileProcessor.BLUEPRINT ) ) ) );
    verify( zipOutputStream ).putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "test-plugin/" ) ) ) );
    verify( zipOutputStream )
      .putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( "test-plugin/other-file" ) ) ) );
  }

  private class ZipEntryMatcher extends ArgumentMatcher<ZipEntry> {
    private final String name;

    public ZipEntryMatcher( ZipEntry zipEntry ) {
      this.name = zipEntry.getName();
    }

    @Override public boolean matches( Object argument ) {
      return argument instanceof ZipEntry && name.equals( ( (ZipEntry) argument ).getName() );
    }
  }

  @Test
  public void testProcessManifest() throws IOException {
    List<PluginFileHandler> pluginFileHandlers =
      new ArrayList<PluginFileHandler>( Arrays.asList( new PluginXmlStaticPathsHandler() ) );

    ZipOutputStream zipOutputStream = mock( ZipOutputStream.class );
    ZipInputStream zipInputStream = mock( ZipInputStream.class );
    PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, true, "test", "test-symbolic", "version" );
    pluginZipFileProcessor.processManifest( zipOutputStream );
    verify( zipOutputStream, times( 1 ) )
      .putNextEntry( argThat( new ZipEntryMatcher( new ZipEntry( JarFile.MANIFEST_NAME ) ) ) );
  }

}
