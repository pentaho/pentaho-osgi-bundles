/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.osgi.manager.resource.api;

import org.osgi.framework.Bundle;

import java.io.File;

/**
 * Created by krivera on 6/21/17.
 */
public interface ResourceProvider {

  /**
   * As a {@link File} this method returns the current managed-resources folder
   *
   * @return A {@link File} of the current managed-resources folder
   */
  public File getManagedResourceFolder();

  /**
   * Provides a folder inside of the managed-resources folder given a {@link Bundle} and the relative path to the folder
   *
   * @param bundle       The {@link Bundle} in which have the managed-resources stored
   * @param relativePath The {@link String} relative path to the resource
   * @return A {@link File} of the provided folder. Null if it does not exist or if it is not a directory
   */
  public File provideFolder( Bundle bundle, String relativePath );

  /**
   * Provides a folder inside of the managed-resources folder given a bundle name and the relative path to the folder
   *
   * @param bundleName   The {@link String} bundle symbolic name of the {@link Bundle} in which have the
   *                     managed-resources stored
   * @param relativePath The {@link String} relative path to the resource
   * @return A {@link File} of the provided folder. Null if it does not exist or if it is not a directory
   */
  public File provideFolder( String bundleName, String relativePath );

  /**
   * Provides a file inside of the managed-resources folder given a {@link Bundle} and the relative path to the file
   *
   * @param bundle       The {@link Bundle} in which have the managed-resources stored
   * @param relativePath The {@link String} relative path to the resource
   * @return A {@link File} of the provided file. Null if it does not exist or if it is not a file
   */
  public File provideFile( Bundle bundle, String relativePath );

  /**
   * Provides a fileinside of the managed-resources folder given a bundle name and the relative path to the file
   *
   * @param bundleName   The {@link String} bundle symbolic name of the {@link Bundle} in which have the
   *                     managed-resources stored
   * @param relativePath The {@link String} relative path to the resource
   * @return A {@link File} of the provided file. Null if it does not exist or if it is not a file
   */
  public File provideFile( String bundleName, String relativePath );
}
