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
package org.pentaho.osgi.platform.plugin.deployer.impl;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by bryan on 8/28/14.
 */
public class PluginZipFileProcessor {
  public static final String BLUEPRINT = "OSGI-INF/blueprint/blueprint.xml";
  public static final String PLUGIN_SPRING_XML_FILENAME = "plugin.spring.xml";

  private Logger logger = LoggerFactory.getLogger( getClass() );
  private final List<PluginFileHandler> pluginFileHandlers;
  private final String name;
  private final String symbolicName;
  private final String version;

  private boolean isPluginProcessedBefore;

  public PluginZipFileProcessor( List<PluginFileHandler> pluginFileHandlers, boolean isPluginProcessedBefore,
                                 String name, String symbolicName,
                                 String version ) {
    this.pluginFileHandlers = pluginFileHandlers != null ? pluginFileHandlers : new ArrayList<>();
    this.name = name;
    this.symbolicName = symbolicName;
    this.version = version;
    this.isPluginProcessedBefore = isPluginProcessedBefore;
  }


  public Future<Void> processBackground( ExecutorService executorService,
                                         final Supplier<ZipInputStream> zipInputStreamProvider,
                                         final ZipOutputStream zipOutputStream,
                                         final ExceptionSettable<Throwable> exceptionSettable ) {
    return executorService.submit( () -> {
      long elapsedTime = System.currentTimeMillis();
      logger.debug( "Start processing zip plugin '{}'", name );

      try {
        if ( isPluginProcessedBefore ) {
          logger.debug( "Found bundle {} installed. Processing manifest instead", name );
          processManifest( zipOutputStream );
        } else {
          process( zipInputStreamProvider, zipOutputStream );
        }
      } catch ( IOException e ) {
        exceptionSettable.setException( e );
      }

      logger.debug( "Finished processing zip plugin '{}'", name );
      logger.debug( "Elapsed time in millis: {} ", ( System.currentTimeMillis() - elapsedTime ) );
      return null;
    } );
  }

  public void process( Supplier<ZipInputStream> zipInputStreamProvider, ZipOutputStream zipOutputStream )
          throws IOException {
    File dir = Files.createTempDir();
    PluginMetadata pluginMetadata = null;
    try {
      pluginMetadata = new PluginMetadataImpl( dir );
    } catch ( ParserConfigurationException e ) {
      throw new IOException( e );
    }
    Manifest manifest = null;
    ZipInputStream zipInputStream = zipInputStreamProvider.get();
    try {
      logger.debug( "Processing plugin - Name: {} SymbolicName: {} Version: {}",
              this.name, this.symbolicName, this.version );
      ZipEntry zipEntry;

      byte[]  pluginSpringXmlBytes = null;
      String pluginSpringXmlName = null;
      while ( ( zipEntry = zipInputStream.getNextEntry() ) != null ) {
        String name = zipEntry.getName();

        byte[] bytes = getEntryBytes( zipInputStream );
        // [BACKLOG-14815]
        // Ensures the plugin.xml file is read before plugin.spring.xml. This is needed so
        // {@link org.pentaho.osgi.platform.plugin.deployer.impl.handlers.SpringFileHandler#handle()}
        // can get the proper bundleName and set the service entry point.
        if ( pluginSpringXmlBytes == null && name != null && name.endsWith( PLUGIN_SPRING_XML_FILENAME ) ) {
          pluginSpringXmlBytes = bytes; // Store plugin.spring.xml for processing after plugin.xml
          pluginSpringXmlName = name;
        } else {
          processEntry( zipOutputStream, pluginMetadata, zipEntry.isDirectory(), name, bytes );
        }
      }

      if ( pluginSpringXmlBytes != null ) {
        processEntry( zipOutputStream, pluginMetadata, false, pluginSpringXmlName, pluginSpringXmlBytes );
      }

    } finally {
      IOUtils.closeQuietly( zipInputStream );
    }



    // Write blueprint to disk, picked up with others later
    int tries = 100;
    File blueprintDir =
            new File( dir.getAbsolutePath() + "/" + BLUEPRINT.substring( 0, BLUEPRINT.lastIndexOf( '/' ) ) );
    while ( !blueprintDir.exists() && tries-- > 0 ) {
      blueprintDir.mkdirs();
    }
    FileOutputStream blueprintOutputStream = null;
    try {
      blueprintOutputStream = new FileOutputStream( dir.getAbsolutePath() + "/" + BLUEPRINT );
      pluginMetadata.writeBlueprint( name, blueprintOutputStream );
    } finally {
      if ( blueprintOutputStream != null ) {
        blueprintOutputStream.close();
      }
    }


    Set<String> createdEntries = new HashSet<String>();

    // 1. Write Manifest Directory
    String manifestFolder = JarFile.MANIFEST_NAME.split( "/" )[ 0 ] + "/";
    ZipEntry manifestFolderEntry = new ZipEntry( manifestFolder );
    zipOutputStream.putNextEntry( manifestFolderEntry );
    zipOutputStream.closeEntry();
    createdEntries.add( manifestFolder );

    // 2. Write Manifest
    ZipEntry manifestEntry = new ZipEntry( JarFile.MANIFEST_NAME );
    zipOutputStream.putNextEntry( manifestEntry );
    pluginMetadata.getManifestUpdater()
            .write( manifest, zipOutputStream, name, symbolicName, version );
    zipOutputStream.closeEntry();
    createdEntries.add( JarFile.MANIFEST_NAME );

    // Handlers may have written files to disk which need to be added.
    Stack<File> dirStack = new Stack<File>();
    dirStack.push( dir );
    int len = 0;
    byte[] buffer = new byte[ 1024 ];
    try {
      while ( dirStack.size() > 0 ) {
        File currentDir = dirStack.pop();
        String dirName = currentDir.getAbsolutePath().substring( dir.getAbsolutePath().length() ) + "/";
        if ( dirName.startsWith( "/" ) || dirName.startsWith( "\\" ) ) {
          dirName = dirName.substring( 1 );
        }
        if ( dirName.length() > 0 && !createdEntries.contains( dirName ) ) {
          ZipEntry zipEntry = new ZipEntry( dirName.replaceAll( Pattern.quote( "\\" ), "/" ) );
          zipOutputStream.putNextEntry( zipEntry );
          zipOutputStream.closeEntry();
        }
        File[] dirFiles = currentDir.listFiles();
        if ( dirFiles != null ) {
          for ( File childFile : dirFiles ) {
            if ( childFile.isDirectory() ) {
              dirStack.push( childFile );
            } else {
              String fileName = childFile.getAbsolutePath().substring( dir.getAbsolutePath().length() + 1 );
              FileInputStream fileInputStream = null;
              try {
                fileInputStream = new FileInputStream( childFile );
                ZipEntry childZipEntry = new ZipEntry( fileName.replaceAll( Pattern.quote( "\\" ), "/" ) );
                zipOutputStream.putNextEntry( childZipEntry );
                while ( ( len = fileInputStream.read( buffer ) ) != -1 ) {
                  zipOutputStream.write( buffer, 0, len );
                }
                zipOutputStream.closeEntry();
              } finally {
                if ( fileInputStream != null ) {
                  fileInputStream.close();
                }
              }
            }
          }
        }
      }
    } finally {
      IOUtils.closeQuietly( zipOutputStream );
      recursiveDelete( dir );
    }
  }

  private void processEntry( ZipOutputStream zipOutputStream, PluginMetadata pluginMetadata,
                             boolean isDirectory, String name, byte[] bytes ) throws IOException {
    logger.trace( "Processing zip entry: {} ", name );
    AtomicBoolean output = new AtomicBoolean( false );
    boolean wasHandled = false;
    for ( PluginFileHandler pluginFileHandler : pluginFileHandlers ) {

      if ( pluginFileHandler.handles( name ) ) {
        wasHandled = true;
        logger.trace( "Plugin file handler {} will handle {}", pluginFileHandler.toString(), name );
        try {
          // There is no short-circuit. Multiple handlers can do work on any given resource
          boolean handlerSaysOutput = pluginFileHandler
                  .handle( name, bytes, pluginMetadata );
          logger.trace( "Plugin file handler {} handled {}", pluginFileHandler.toString(), name );
          output.compareAndSet( false, handlerSaysOutput );
        } catch ( PluginHandlingException e ) {
          logger.error( "Plugin file handler " + pluginFileHandler.toString() + " threw exception when handling "
                  + name, e );
          throw new IOException( e );
        }
      }
    }
    if ( !wasHandled || output.get() ) {
      zipOutputStream.putNextEntry( new ZipEntry( name ) );
      if ( isDirectory == false ) {
        IOUtils.write( bytes, zipOutputStream );
      }
      zipOutputStream.closeEntry();
    }
  }

  private byte[] getEntryBytes( ZipInputStream zipInputStream ) throws IOException {

    return IOUtils.toByteArray( zipInputStream );

  }

  public void processManifest( ZipOutputStream zipOutputStream ) throws IOException {
    Manifest manifest = null;

    String manifestFolder = JarFile.MANIFEST_NAME.split( "/" )[ 0 ] + "/";
    ZipEntry manifestFolderEntry = new ZipEntry( manifestFolder );
    zipOutputStream.putNextEntry( manifestFolderEntry );
    zipOutputStream.closeEntry();

    ZipEntry manifestEntry = new ZipEntry( JarFile.MANIFEST_NAME );
    zipOutputStream.putNextEntry( manifestEntry );
    new ManifestUpdaterImpl().write( manifest, zipOutputStream, name, symbolicName, version );
    zipOutputStream.closeEntry();

    zipOutputStream.flush();

    zipOutputStream.close();
  }

  private void recursiveDelete( File file ) {
    if ( file.isDirectory() ) {
      for ( File child : file.listFiles() ) {
        recursiveDelete( child );
      }
    }
    if ( !file.delete() ) {
      file.deleteOnExit();
    }
  }
}
