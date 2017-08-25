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
package org.pentaho.osgi.manager.resource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.pentaho.osgi.manager.resource.api.ResourceProvider;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by krivera on 6/21/17.
 */
public class ManagedResourceProvider implements ResourceProvider {
  private BundleContext bundleContext;
  private File managedResourcesFolder;

  private static String ROOT_SYSTEM_DIR = File.separator + "system";
  public static String MANAGED_RESOURCES_DIR = File.separator + "managed-resources";

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void init() {
    managedResourcesFolder = findManagedResourcesFolder( bundleContext.getBundle() );
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
   * Iterates through parent folders until the root 'system' folder is reached
   *
   * @param blueprintBundle - The current blue print bundle
   * @return A {@link File} that is the root of /managed-resources folder
   */
  protected File findManagedResourcesFolder( Bundle blueprintBundle ) {
    File root = blueprintBundle.getDataFile( "" ); // Path to root of bundle
    while ( root != null && !root.getAbsolutePath().endsWith( ROOT_SYSTEM_DIR ) ) {
      root = root.getParentFile();
    }

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
