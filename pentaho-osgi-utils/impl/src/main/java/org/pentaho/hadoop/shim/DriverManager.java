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

import org.osgi.framework.BundleContext;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
  private static final String DRIVER_FILE_EXTENSION_KAR = ".kar";
  private static final String DRIVER_FILE_EXTENSION_ZIP = ".zip";

  private static final String BIG_DATA_PLUGIN_ID = "HadoopSpoonPlugin";
  private final String BIG_DATA_PLUGIN_DIR;

  private DriverManager() {
    BIG_DATA_PLUGIN_DIR = PluginRegistry.getInstance().getPlugin( LifecyclePluginType.class, BIG_DATA_PLUGIN_ID ).getPluginDirectory().getPath() + "/drivers";
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
    logger.info( "Installing big data drivers." );
    Stream<Path> driverFileList = null;
    try {
      String driverSourceDirName = Const.getShimDriverDeploymentLocation();
      File driverSourceDir = new File( driverSourceDirName );

      if ( !driverSourceDir.exists() ) {
        String logMessage = String.format( "Drivers installation directory not found: %s", driverSourceDirName );
        logger.info( logMessage );
        return;
      }
      driverFileList = Files.list( driverSourceDir.toPath() );
      List<String> driversInDir = driverFileList
        .filter( path -> ( path.toString().endsWith( DRIVER_FILE_EXTENSION_KAR ) || path.toString().endsWith( DRIVER_FILE_EXTENSION_ZIP ) ) )
        .map( path -> path.getFileName().toString() )
        .collect( Collectors.toList() );
      List<String> driversToInstall = new ArrayList( driversInDir );

      String driverInstallCountMsg = String.format( "%d drivers will be installed.", driversToInstall.size() );
      logger.info( driverInstallCountMsg );

      driversToInstall.stream()
        .forEach( driverName -> {
          try {
            logger.info( String.format( "Installing %s", driverName ) );
            processDriverFile( driverName, driverSourceDir.getAbsolutePath() );
            logger.info( String.format( "%s driver installed.", driverName ) );
          } catch ( Exception e ) {
            logger.error( "Failed installing driver: " + driverName, e );
          }
        } );
    } catch ( Exception e ) {
      logger.error( "Failed driver installation process.", e );
    } finally {
      if ( driverFileList != null ) {
        driverFileList.close();
      }
    }
    logger.info( "Finished installing big data drivers." );
  }

  private void processDriverFile( String driverName, String driverDirectory ) throws Exception {
    if ( driverName.endsWith( DRIVER_FILE_EXTENSION_ZIP ) ) {
      DriverZipUtil.unzipFile( driverDirectory + "/" + driverName, BIG_DATA_PLUGIN_DIR );
    } else if ( driverName.endsWith( DRIVER_FILE_EXTENSION_KAR ) ) {
      DriverKarUtil.processKarFile( driverName, driverDirectory, BIG_DATA_PLUGIN_DIR );
    } else {
      throw new Exception( "Failed processing driver file. Wrong file extension: " + driverDirectory );
    }
  }

}
