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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by krivera on 6/23/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class ManagedResourceProviderTest {

  @Spy ManagedResourceProvider resourceProvider;
  @Mock Bundle bundle;
  String karafPath = Paths.get( ".", "path", "to", "karaf" ).toString();
  @Mock BundleContext bundleContext;
  String bundleName = "test-bundle-name";
  String relativePath = Paths.get( "path", "to", "file" ).toString();
  String expectedRelativePath =
    Paths.get( karafPath + ManagedResourceProvider.MANAGED_RESOURCES_DIR + File.separator + bundleName ).toString();
  String fileName = Paths.get( "test.csv" ).toString();

  @Before public void setup() throws IOException {
    System.setProperty( "karaf.home", karafPath );

    ( Paths.get( expectedRelativePath, relativePath ).toFile() ).mkdirs();
    ( Paths.get( expectedRelativePath, relativePath, fileName ).toFile() ).createNewFile();

    doReturn( bundleName ).when( bundle ).getSymbolicName();

    resourceProvider.setBundleContext( bundleContext );
    resourceProvider.init();
  }

  @After public void teardown() throws IOException {
    File toDelete = new File( "./path" );
    FileUtils.deleteDirectory( toDelete );
  }

  @Test public void findManagedResourcesFolderTest() {
    File managedResourceFolder = resourceProvider.findManagedResourcesFolder();
    assertTrue(
      managedResourceFolder.getAbsolutePath().contains( karafPath + ManagedResourceProvider.MANAGED_RESOURCES_DIR ) );
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
    assertNull( resourceProvider.validateFolder( null ) );

    doReturn( true ).when( folder ).exists();
    assertNull( resourceProvider.validateFolder( null ) );

    doReturn( true ).when( folder ).isDirectory();
    assertEquals( folder, resourceProvider.validateFolder( folder ) );
  }

  @Test public void validateFileTest() {
    assertNull( resourceProvider.validateFile( null ) );

    File file = mock( File.class );
    assertNull( resourceProvider.validateFile( null ) );

    doReturn( true ).when( file ).exists();
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
