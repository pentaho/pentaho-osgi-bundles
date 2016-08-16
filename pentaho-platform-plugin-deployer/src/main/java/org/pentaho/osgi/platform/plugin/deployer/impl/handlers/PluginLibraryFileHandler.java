/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
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
 */

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import com.google.common.io.ByteStreams;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Created by nbaker on 7/18/16.
 */
public class PluginLibraryFileHandler implements PluginFileHandler {

  private static Logger log = LoggerFactory.getLogger( PluginLibraryFileHandler.class );

  public static final Pattern LIB_PATTERN = Pattern.compile( ".+\\/lib\\/.+\\.jar"  );
  public static final String JAR = ".jar";

  @Override public boolean handles( String fileName ) {
    return LIB_PATTERN.matcher( fileName ).matches();
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
//    pluginMetadata.getManifestUpdater().getClasspathEntries().add( relativePath );
    FileInputStream fin = null;
    JarInputStream jarInputStream = null;
    try {
      fin = new FileInputStream( file );
      jarInputStream = new JarInputStream( fin );

      Object bundleSymbolicName = jarInputStream.getManifest().getMainAttributes().getValue( "Bundle-SymbolicName" );
      if ( bundleSymbolicName != null ) {
        // don't load a jar that is already provided as a bundle in the system.
        // If it has a Bundle-SymbolicName, it's assumed that we provide it already in the OSGi container
        log.info( String.format(
          "Jar identified as an OSGi bundle [%s]; no auto-deploy. Assumed to be provided by the container.",
          bundleSymbolicName.toString() ) );
        return;
      }

      ZipEntry nextEntry;
      while ( ( nextEntry = jarInputStream.getNextEntry() ) != null ) {
        if ( nextEntry.isDirectory() ) {
          continue;
        }
        String name = nextEntry.getName();
        ByteArrayOutputStream byteArrayOutputStream = null;

        // Get the contents
        try {
          byteArrayOutputStream =
              new ByteArrayOutputStream( (int) Math.min( Integer.MAX_VALUE, Math.max( 0, nextEntry.getSize() ) ) );
          ByteStreams.copy( jarInputStream, byteArrayOutputStream );

        } finally {
          byteArrayOutputStream.close();
        }

        if ( name.matches( ".+\\.xml" ) ) {
          String contents = byteArrayOutputStream.toString( "UTF-8" );
          if ( contents.contains( "http://www.springframework.org/schema/beans" ) ) {
            // It is a spring file. move it to SpringDM location
            FileWriter fileWriter = pluginMetadata.getFileWriter( "META-INF/spring/" + name );
            fileWriter.append( contents );
            fileWriter.close();
            continue;
          }
        }
        OutputStream fileOut = pluginMetadata.getFileOutputStream( nextEntry.getName() );
        fileOut.write( byteArrayOutputStream.toByteArray() );
        fileOut.close();

      }
    } catch ( IOException e ) {
      e.printStackTrace();
    } finally {
      try {
        jarInputStream.close();
        fin.close();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }
}
