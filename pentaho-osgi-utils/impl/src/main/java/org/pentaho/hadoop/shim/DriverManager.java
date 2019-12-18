/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.hadoop.shim;

import org.apache.karaf.kar.KarService;
import org.osgi.framework.BundleContext;
import org.pentaho.di.core.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DriverManager {

  private static DriverManager driverManagerInstance;
  private BundleContext bundleContext;
  private static Logger logger = LoggerFactory.getLogger( DriverManager.class );

  public static final String CONFIG_FILE_NAME = "org.pentaho.features";
  public static final String INSTALL_DRIVERS_PROPERTY = "installDrivers";
  private static final String DRIVER_FILE_EXTENSION = ".kar";

  private DriverManager() {
  }

  /**
   * Method will return the DriverManager instance configured with the given bundle context.
   *
   * @param bundleContext - bundleContext to be used by the DriverManager to get the KarService instance
   * @return - DriverManager instance configured with the given bundle context.
   */
  public static DriverManager getInstance( BundleContext bundleContext ) {
    if ( driverManagerInstance == null ) {
      driverManagerInstance = new DriverManager();
    }
    driverManagerInstance.setBundleContext( bundleContext );
    return driverManagerInstance;
  }

  /**
   * Set the bundleContext to be used by the DriverManager to get the KarService instance
   *
   * @param bundleContext
   */
  private void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  /**
   * Method to install the shim drivers from a given set of Kar files. The location to be searched is configurable using
   * the kettle property Const.SHIM_DRIVER_DEPLOYMENT_LOCATION. If a Kar file with the same name if already registered
   * by the KarService, that Kar file will not be installed and will be assumed installed. To refresh or re-install the
   * Kar files in the drivers directory, clear the Karaf catch and restart the application. To remove an installed Kar
   * file, delete the Kar file from the drivers directory, clear the Karaf catch and restart the application.
   */
  public void installDrivers() {
    logger.info( "Installing driver kars." );
    KarService karService;
    Stream<Path> karFileList = null;
    try {
      String karSourceDirName = Const.getShimDriverDeploymentLocation();
      File karSourceDir = new File( karSourceDirName );

      if ( !karSourceDir.exists() ) {
        String logMessage = String.format( "Drivers installation directory not found: %s", karSourceDirName );
        logger.info( logMessage );
        return;
      }
      karService =
        (KarService) bundleContext.getService( bundleContext.getServiceReference( "org.apache.karaf.kar.KarService" ) );
      karFileList = Files.list( karSourceDir.toPath() );
      List<String> karsInDir = karFileList
        .filter( path -> path.toString().endsWith( DRIVER_FILE_EXTENSION ) )
        .map( path -> path.getFileName().toString().substring( 0, path.getFileName().toString().length() - 4 ) )
        .collect( Collectors.toList() );
      List<String> karsToInstall = new ArrayList( karsInDir );
      karsToInstall.removeAll( karService.list() );

      String driverInstallCountMsg = String.format( "%d drivers will be installed.", karsToInstall.size() );
      logger.info( driverInstallCountMsg );

      karsToInstall.stream()
        .forEach( karname -> {
          try {
            logger.info( String.format( "Installing %s", karname ) );
            Path karPath = Paths.get( karSourceDir.getAbsolutePath(), karname + DRIVER_FILE_EXTENSION );
            karService.install( karPath.toUri() );
            logger.info( String.format( "%s Kar installed.", karname ) );
          } catch ( Exception e ) {
            logger.error( "Failed installing driver: " + karname + DRIVER_FILE_EXTENSION, e );
          }
        } );
    } catch ( Exception e ) {
      logger.error( "Failed driver installation process.", e );
    } finally {
      if ( karFileList != null ) {
        karFileList.close();
      }
    }
    logger.info( "Finished installing drivers kars." );
  }
}
