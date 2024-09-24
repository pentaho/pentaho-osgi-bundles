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
