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
