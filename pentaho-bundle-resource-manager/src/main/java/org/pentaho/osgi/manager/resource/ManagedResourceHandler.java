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

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.pentaho.osgi.manager.resource.api.ResourceHandler;
import org.pentaho.osgi.manager.resource.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 * Created by krivera on 6/15/17.
 */
public class ManagedResourceHandler implements ResourceHandler {

  private ResourceProvider managedResourceProvider;
  private Logger logger = LoggerFactory.getLogger( ManagedResourceHandler.class );
  public static String BUNDLE_MANAGED_RESOURCES_DIR = "/managed-resources";

  public void setManagedResourceProvider( ResourceProvider resourceProvider ) {
    this.managedResourceProvider = resourceProvider;
  }

  @Override public boolean hasManagedResources( Bundle bundle ) {
    try {
      return bundle.getResources( BUNDLE_MANAGED_RESOURCES_DIR ) != null;
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override public void handleManagedResources( Bundle blueprintBundle ) {
    File to = getOutputDirectory( blueprintBundle );
    writeFilesToDisk( blueprintBundle, BUNDLE_MANAGED_RESOURCES_DIR, to );
  }

  /**
   * Iterates through parent folders until the root 'system' folder is reached
   *
   * @param blueprintBundle - The current blue print bundle
   * @return
   */
  protected File getOutputDirectory( Bundle blueprintBundle ) {
    File managedResourcesFolder = managedResourceProvider.getManagedResourceFolder();
    File to = Paths.get( managedResourcesFolder.getAbsolutePath(), blueprintBundle.getSymbolicName() ).toFile();

    if ( !to.exists() ) {
      to.mkdirs();
    }

    return to;
  }

  /**
   * Writes files within the blueprint to the associated data folder on the hard disk
   *
   * @param bundle       The current blueprint bundle
   * @param bundleSource The directory within the bundle from which the files are written
   * @param to           The {@link File} where the resources will be written to
   * @return Return whether the operation was successful
   */
  protected boolean writeFilesToDisk( Bundle bundle, String bundleSource, File to ) {
    InputStream in = null;
    OutputStream out = null;

    try {
      Enumeration<URL> fileUrls = bundle.findEntries( bundleSource, null, true );
      if ( fileUrls == null || !fileUrls.hasMoreElements() ) {
        return false;
      }

      while ( fileUrls.hasMoreElements() ) { // Loop through file urls
        URL url = fileUrls.nextElement();

        Path outPath = Paths
          .get( to.getPath(), url.getFile().replace( BUNDLE_MANAGED_RESOURCES_DIR, "" ) );
        File outFile = outPath.toFile();
        String fileName = outPath.getFileName().toString();

        if ( !outFile.exists() ) { // Check if the file is already written
          if ( fileName.matches( ".+\\..+" ) ) { // Is File
            copyStream( url, outFile.getAbsolutePath() ); // Write file
          } else { // Is Directory
            outFile.mkdir(); // Create directory
          }
        }
      }
    } catch ( FileNotFoundException e ) {
      logger.error( e.getMessage() );
      return false;
    } catch ( IOException e ) {
      logger.error( e.getMessage() );
      return false;
    } finally {
      try {
        if ( in != null ) {
          in.close();
        }
      } catch ( IOException e ) {
        logger.error( e.getMessage() );
      }

      try {
        if ( out != null ) {
          out.close();
        }
      } catch ( IOException e ) {
        logger.error( e.getMessage() );
      }
    }

    return true;
  }

  /**
   * For testing, this method handles copying an {@link InputStream} to an {@link OutputStream}
   *
   * @param inUrl          {@link URL} of the source file
   * @param outAbsolutPath {@link String} path of the destination file
   * @throws IOException
   */
  protected void copyStream( URL inUrl, String outAbsolutPath ) throws IOException {
    InputStream in = inUrl.openStream();
    OutputStream out = new FileOutputStream( outAbsolutPath );
    IOUtils.copy( in, out );
  }
}
