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
package org.pentaho.osgi.manager.resource;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by krivera on 6/22/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class ManagedResourceHandlerTest {
  private String fileName = "file.abc";
  private String filePath = Paths.get( ".", "some", "file", "url" ).toString();
  private String fileUrl = Paths.get( filePath, fileName ).toString();

  private String bundleSource = Paths.get( "test", "source", "path.csv" ).toString();
  private String source = File.pathSeparator + bundleSource;

  private File file;
  private File to;

  @Mock Bundle bundle;
  @Spy ManagedResourceHandler resourceHandler;
  @Mock ManagedResourceProvider resourceProvider;
  String bundleSymbolicName = "test-osgi-bundle";
  File expectedOutputDirectory;
  String absoluteManagedResource = Paths.get( ".", "path", "to", "absolute", "managed", "resources" ).toString();

  @Before public void setup() throws Exception {
    file = Paths.get( fileUrl ).toFile();
    file.mkdirs();
    file.createNewFile();

    to = Paths.get( "." ).toFile();
    doReturn( bundleSymbolicName ).when( bundle ).getSymbolicName();

    File file = mock( File.class );

    doReturn( absoluteManagedResource ).when( file ).getAbsolutePath();
    doReturn( file ).when( resourceProvider ).getManagedResourceFolder();

    expectedOutputDirectory = new File( absoluteManagedResource + File.separator + bundleSymbolicName );
    resourceHandler.setManagedResourceProvider( resourceProvider );
    doCallRealMethod().when( resourceHandler ).writeFilesToDisk( bundle, source, to );
  }

  @After public void teardown() throws IOException {
    File rootExpectedOutputDirectory =
      Paths.get( "." + File.separator + absoluteManagedResource.substring( 2 ).split( File.separator )[ 0 ] ).toFile();
    FileUtils.deleteDirectory( rootExpectedOutputDirectory );

    File toDelete = Paths.get( "." + File.separator + fileUrl.substring( 2 ).split( File.separator )[ 0 ] ).toFile();
    FileUtils.deleteDirectory( toDelete );
  }

  @Test public void writeFilesToDiskTestWithNullBundleDirectory() throws Exception {
    boolean result = resourceHandler.writeFilesToDisk( bundle, source, to );
    assertFalse( result );
  }

  @Test public void writeFilesToDiskTest() throws Exception {
    doNothing().when( resourceHandler ).copyStream( any( URL.class ), anyString() );

    Enumeration<URL> mockFileUrls = mock( Enumeration.class );
    doReturn( mockFileUrls ).when( bundle ).findEntries( source, null, true );

    doReturn( false ).when( mockFileUrls ).hasMoreElements();

    boolean result = resourceHandler.writeFilesToDisk( bundle, source, to );
    assertFalse( result );

    // Test 1 - test write occurs when no files exist
    URL rootJarUrl = resourceHandler.getClass().getResource( "" );

    Set<URL> urls = new HashSet<>();
    urls.add( rootJarUrl );
    urls.add( Paths.get( bundleSource, "test.abc" ).toUri().toURL() );

    Iterator<URL> urlIterator = urls.iterator();
    doAnswer( invocationOnMock -> urlIterator.hasNext() ).when( mockFileUrls ).hasMoreElements();
    doAnswer( invocationOnMock -> urlIterator.next() ).when( mockFileUrls ).nextElement();

    result = resourceHandler.writeFilesToDisk( bundle, source, to );
    assertTrue( result );
    verify( resourceHandler, times( 1 ) ).copyStream( any( URL.class ), any( String.class ) );

    // Test 2 - test if files already exist
    urls.clear();
    urls.add( new URL( "file", "", fileUrl ) );

    Iterator<URL> urlIterator1 = urls.iterator();
    doAnswer( invocationOnMock -> urlIterator1.hasNext() ).when( mockFileUrls ).hasMoreElements();
    doAnswer( invocationOnMock -> urlIterator1.next() ).when( mockFileUrls ).nextElement();

    result = resourceHandler.writeFilesToDisk( bundle, source, to );
    assertTrue( result );
    verify( resourceHandler, times( 1 ) ).copyStream( any( URL.class ), any( String.class ) );
  }

  @Test public void testHasManagedResources() throws Exception {
    doReturn( mock( Enumeration.class ) ).when( bundle )
      .getResources( ManagedResourceHandler.BUNDLE_MANAGED_RESOURCES_DIR );
    assertTrue( resourceHandler.hasManagedResources( bundle ) );
  }

  @Test public void testDoesNotHaveManagedResources() throws Exception {
    doReturn( null ).when( bundle ).getResources( ManagedResourceHandler.BUNDLE_MANAGED_RESOURCES_DIR );
    assertFalse( resourceHandler.hasManagedResources( bundle ) );
  }

  @Test public void testGetOutputDirectory() {
    assertFalse( expectedOutputDirectory.exists() );

    File result = resourceHandler.getOutputDirectory( bundle );
    assertEquals( expectedOutputDirectory.getAbsoluteFile(), result.getAbsoluteFile() );
    assertTrue( expectedOutputDirectory.exists() );
  }

  @Test public void testHandleManagedResources() throws Exception {
    resourceHandler.handleManagedResources( bundle );
    verify( resourceHandler ).getOutputDirectory( bundle );
    verify( resourceHandler )
      .writeFilesToDisk( eq( bundle ), eq( ManagedResourceHandler.BUNDLE_MANAGED_RESOURCES_DIR ), any( File.class ) );
  }
}
