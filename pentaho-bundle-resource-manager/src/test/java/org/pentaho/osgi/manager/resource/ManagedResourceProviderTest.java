/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.osgi.manager.resource;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by krivera on 6/23/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class ManagedResourceProviderTest {

  @Spy ManagedResourceProvider resourceProvider;
  @Mock Bundle bundle;
  String systemPath = Paths.get( ".", "path", "to", "system" ).toString();
  String bundlePath = Paths.get( systemPath, "bundle" ).toString();
  File bundleLocationFile;
  @Mock BundleContext bundleContext;
  String bundleName = "test-bundle-name";
  String relativePath = Paths.get( "path", "to", "file" ).toString();
  String expectedRelativePath =
    Paths.get( systemPath + ManagedResourceProvider.MANAGED_RESOURCES_DIR + File.separator + bundleName ).toString();
  String fileName = Paths.get( "test.csv" ).toString();

  @Before public void setup() throws IOException {
    bundleLocationFile = new File( bundlePath );
    bundleLocationFile.mkdirs();


    ( Paths.get( expectedRelativePath, relativePath ).toFile() ).mkdirs();
    ( Paths.get( expectedRelativePath, relativePath, fileName ).toFile() ).createNewFile();

    doReturn( bundleLocationFile ).when( bundle ).getDataFile( anyString() );
    doReturn( bundleName ).when( bundle ).getSymbolicName();
    doReturn( bundle ).when( bundleContext ).getBundle();

    resourceProvider.setBundleContext( bundleContext );
    resourceProvider.init();
  }

  @After public void teardown() throws IOException {
    File toDelete = new File( "./" + bundlePath.substring( 2 ).split( "/" )[ 0 ] );
    FileUtils.deleteDirectory( toDelete );
  }

  @Test public void findManagedResourcesFolderTest() {
    File managedResourceFolder = resourceProvider.findManagedResourcesFolder( bundle );
    assertTrue(
      managedResourceFolder.getAbsolutePath().contains( systemPath + ManagedResourceProvider.MANAGED_RESOURCES_DIR ) );
  }

  @Test public void getAbsoluteFileTest() {
    Bundle nullBundle = null;
    assertNull( resourceProvider.getAbsoluteFile( nullBundle, relativePath ) );

    File absoluteFile = resourceProvider.getAbsoluteFile( bundleName, relativePath );
    assertTrue( absoluteFile.getAbsolutePath().contains( expectedRelativePath ) );

    relativePath = ManagedResourceProvider.MANAGED_RESOURCES_DIR + relativePath;
    absoluteFile = resourceProvider.getAbsoluteFile( bundleName, relativePath );
    assertTrue( absoluteFile.getAbsolutePath().contains( expectedRelativePath ) );

    absoluteFile = resourceProvider.getAbsoluteFile( bundle, relativePath );
    assertTrue( absoluteFile.getAbsolutePath().contains( expectedRelativePath ) );
  }

  @Test public void validateFolderTest() {
    assertNull( resourceProvider.validateFolder( null ) );

    File folder = mock( File.class );
    doReturn( false ).when( folder ).exists();
    assertNull( resourceProvider.validateFolder( null ) );

    doReturn( true ).when( folder ).exists();
    doReturn( false ).when( folder ).isDirectory();
    assertNull( resourceProvider.validateFolder( null ) );

    doReturn( true ).when( folder ).isDirectory();
    assertEquals( folder, resourceProvider.validateFolder( folder ) );
  }

  @Test public void validateFileTest() {
    assertNull( resourceProvider.validateFile( null ) );

    File file = mock( File.class );
    doReturn( false ).when( file ).exists();
    assertNull( resourceProvider.validateFile( null ) );

    doReturn( true ).when( file ).exists();
    doReturn( false ).when( file ).isDirectory();
    assertNull( resourceProvider.validateFile( null ) );

    doReturn( true ).when( file ).isFile();
    assertEquals( file, resourceProvider.validateFile( file ) );
  }

  @Test public void provideFolderTest() {
    Bundle nullBundle = null; // Required so that call is not ambiguous
    assertNull( resourceProvider.provideFolder( nullBundle, null ) );

    File providedFolder = resourceProvider.provideFolder( bundle, relativePath );
    assertTrue( providedFolder.getAbsolutePath().contains( expectedRelativePath ) );

    providedFolder = resourceProvider.provideFolder( bundleName, relativePath );
    assertTrue( providedFolder.getAbsolutePath().contains( expectedRelativePath ) );
  }

  @Test public void provideFileTest() {
    Bundle nullBundle = null; // Required so that call is not ambiguous
    assertNull( resourceProvider.provideFile( nullBundle, null ) );

    File providedFile = resourceProvider.provideFile( bundle, Paths.get( relativePath, fileName ).toString() );
    assertTrue( providedFile.getAbsolutePath().contains( expectedRelativePath ) );

    providedFile = resourceProvider.provideFile( bundleName, Paths.get( relativePath, fileName ).toString() );
    assertTrue( providedFile.getAbsolutePath().contains( expectedRelativePath ) );
  }
}
