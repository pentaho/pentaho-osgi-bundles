/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl;

import com.google.common.io.Files;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
  public static final String BLUEPRINT_REGEX = ".*\\/OSGI-INF\\/blueprint\\/.*\\.xml";
  public static final String MANIFEST_REGEX = ".*\\/META-INF\\/MANIFEST.MF";

  private final List<PluginFileHandler> pluginFileHandlers;
  private final String name;
  private final String symbolicName;
  private final String version;

  public PluginZipFileProcessor( List<PluginFileHandler> pluginFileHandlers, String name, String symbolicName,
                                 String version ) {
    this.pluginFileHandlers = pluginFileHandlers;
    this.name = name;
    this.symbolicName = symbolicName;
    this.version = version;
  }

  public Future<Void> processBackground( ExecutorService executorService, final ZipInputStream zipInputStream,
                                         final ZipOutputStream zipOutputStream,
                                         final ExceptionSettable<IOException> exceptionSettable ) {
    return executorService.submit( new Callable<Void>() {
      @Override public Void call() throws Exception {
        try {
          process( zipInputStream, zipOutputStream );
        } catch ( IOException e ) {
          exceptionSettable.setException( e );
        }
        return null;
      }
    } );
  }

  public void process( ZipInputStream zipInputStream, ZipOutputStream zipOutputStream ) throws IOException {
    File dir = Files.createTempDir();
    PluginMetadata pluginMetadata = null;
    try {
      pluginMetadata = new PluginMetadataImpl( dir );
    } catch ( ParserConfigurationException e ) {
      throw new IOException( e );
    }
    Manifest manifest = null;
    try {
      ZipEntry zipEntry = null;
      Document blueprint = null;
      while ( ( zipEntry = zipInputStream.getNextEntry() ) != null ) {
        ByteArrayOutputStream byteArrayOutputStream =
          new ByteArrayOutputStream( (int) Math.max( 0, Math.min( Integer.MAX_VALUE, zipEntry.getSize() ) ) );
        byte[] buffer = new byte[ 1024 ];
        int read;
        while ( ( read = zipInputStream.read( buffer ) ) > 0 ) {
          byteArrayOutputStream.write( buffer, 0, read );
        }
        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        String name = zipEntry.getName();
        boolean shouldOutput = true;
        if ( name.matches( MANIFEST_REGEX ) ) {
          shouldOutput = false;
          manifest = new Manifest( new ByteArrayInputStream( zipBytes ) );
        } else if ( name.matches( BLUEPRINT_REGEX ) ) {
          shouldOutput = false;
          try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware( true );
            blueprint =
              documentBuilderFactory.newDocumentBuilder().parse( new ByteArrayInputStream( zipBytes ) );
            pluginMetadata.setBlueprint( blueprint );
          } catch ( Exception e ) {
            throw new IOException( e );
          }
        }
        if ( shouldOutput ) {
          File outFile = new File( dir.getAbsolutePath() + "/" + zipEntry.getName() );
          int tries = 100;
          File outParent = outFile.getParentFile();
          while ( !outParent.exists() && tries-- > 0 ) {
            outParent.mkdirs();
          }
          if ( zipEntry.isDirectory() ) {
            tries = 100;
            while ( !outFile.exists() && tries-- > 0 ) {
              outFile.mkdir();
            }
          } else {
            FileOutputStream fileOutputStream = null;
            try {
              fileOutputStream = new FileOutputStream( outFile );
              int len = 0;
              fileOutputStream.write( zipBytes );
            } finally {
              if ( fileOutputStream != null ) {
                fileOutputStream.close();
              }
            }
          }
        }
      }

      if ( pluginFileHandlers != null ) {
        Stack<File> fileStack = new Stack<File>();
        fileStack.push( dir );
        while ( fileStack.size() > 0 ) {
          File currentFile = fileStack.pop();
          File searchFile = currentFile;
          Stack<String> dirStack = new Stack<String>();
          while ( !searchFile.equals( dir ) ) {
            dirStack.push( searchFile.getName() );
            searchFile = searchFile.getParentFile();
          }
          String currentFileName = null;
          StringBuilder sb = new StringBuilder();
          while ( dirStack.size() > 0 ) {
            sb.append( dirStack.pop() );
            sb.append( "/" );
          }
          if ( sb.length() > 0 ) {
            sb.setLength( sb.length() - 1 );
          }
          String currentPath = sb.toString();
          for ( PluginFileHandler pluginFileHandler : pluginFileHandlers ) {
            if ( pluginFileHandler.handles( currentPath ) ) {
              try {
                // There is no short-circuit. Multiple handlers can do work on any given resource
                pluginFileHandler.handle( currentPath, currentFile, pluginMetadata );
              } catch ( PluginHandlingException e ) {
                throw new IOException( e );
              }
            }
          }

          if ( currentFile.isDirectory() ) {
            File[] dirFiles = currentFile.listFiles();
            for ( File file : dirFiles ) {
              fileStack.push( file );
            }
          }
        }
      }

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
    } finally {
      try {
        zipInputStream.close();
      } catch ( IOException e ) {
        //Noop
      }
    }
    Set<String> createdEntries = new HashSet<String>();

    String manifestFolder = JarFile.MANIFEST_NAME.split( "/" )[ 0 ] + "/";
    ZipEntry manifestFolderEntry = new ZipEntry( manifestFolder );
    zipOutputStream.putNextEntry( manifestFolderEntry );
    zipOutputStream.closeEntry();
    createdEntries.add( manifestFolder );

    ZipEntry manifestEntry = new ZipEntry( JarFile.MANIFEST_NAME );
    zipOutputStream.putNextEntry( manifestEntry );
    pluginMetadata.getManifestUpdater()
      .write( manifest, zipOutputStream, name, symbolicName, version );
    zipOutputStream.closeEntry();
    createdEntries.add( JarFile.MANIFEST_NAME );

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
      try {
        zipOutputStream.close();
      } catch ( IOException e ) {
        // Noop
      }
      recursiveDelete( dir );
    }
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

