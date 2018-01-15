/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.nio.file.Paths;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.pentaho.osgi.manager.resource.api.ResourceProvider;

/**
 * Created by krivera on 6/21/17.
 */
public class ManagedResourceProvider implements ResourceProvider {
  private BundleContext bundleContext;
  private File managedResourcesFolder;

  public static String MANAGED_RESOURCES_DIR = File.separator + "managed-resources";

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void init() {
    managedResourcesFolder = findManagedResourcesFolder();
  }

  @Override public File getManagedResourceFolder() {
    return this.managedResourcesFolder;
  }

  @Override public File provideFolder( Bundle bundle, String relativePath ) {
    File folder = getAbsoluteFile( bundle, relativePath );
    return validateFolder( folder );
  }

  @Override public File provideFolder( String bundleName, String relativePath ) {
    File folder = getAbsoluteFile( bundleName, relativePath );
    return validateFolder( folder );
  }

  @Override public File provideFile( Bundle bundle, String relativePath ) {
    File file = getAbsoluteFile( bundle, relativePath );
    return validateFile( file );
  }

  @Override public File provideFile( String bundleName, String relativePath ) {
    File file = getAbsoluteFile( bundleName, relativePath );
    return validateFile( file );
  }

  /**
   * Validates the {@link File} for multiple criteria to be a folder
   *
   * @param folder The {@link File} being validated
   * @return null if the {@link File} does not pass the criteria, otherwise the {@link File} is returned
   */
  protected File validateFolder( File folder ) {
    return folder != null && folder.exists() && folder.isDirectory() ? folder : null;
  }

  /**
   * Validates the {@link File} for multiple criteria to be a file
   *
   * @param file The {@link File} being validated
   * @return null if the {@link File} does not pass the criteria, otherwise the {@link File} is returned
   */
  protected File validateFile( File file ) {
    return file != null && file.exists() && file.isFile() ? file : null;
  }

  /**
   * Constructs path to managed resources' root folder relative to karaf's home
   *
   * @return A {@link File} that is the root of /managed-resources folder
   */
  protected File findManagedResourcesFolder( ) {
    Preconditions.checkState( !Strings.isNullOrEmpty( System.getProperty( "karaf.home" ) ),
        "karaf.home system property is not set" );
    File root = new File( System.getProperty( "karaf.home" ) );

    return Paths.get( root.getAbsolutePath(), MANAGED_RESOURCES_DIR ).toFile();
  }

  /**
   * Returns a file provided a {@link Bundle} and a relative path inside of the managed-resources folder
   *
   * @param bundle       The {@link Bundle} which stored the original managed-resources
   * @param relativePath The {@link String} relative path inside of the managed-resources folder
   * @return A {@link File} at the absolute path comprised of the bundle symbolic name and the relative path inside of
   * the managed-resources folder
   */
  protected File getAbsoluteFile( Bundle bundle, String relativePath ) {
    return bundle == null ? null : getAbsoluteFile( bundle.getSymbolicName(), relativePath );
  }

  /**
   * Returns a file provided a bundle name and a relative path inside of the managed-resources folder
   *
   * @param bundleName   The symbolic name of the {@link Bundle} which stored the original managed-resources
   * @param relativePath The {@link String} relative path inside of the managed-resources folder
   * @return A {@link File} at the absolute path comprised of the bundle symbolic name and the relative path inside of
   * the managed-resources folder
   */
  protected File getAbsoluteFile( String bundleName, String relativePath ) {
    return Paths.get( this.managedResourcesFolder.getPath(), bundleName,
      relativePath.replace( MANAGED_RESOURCES_DIR, "" ) )
      .toFile();
  }
}
