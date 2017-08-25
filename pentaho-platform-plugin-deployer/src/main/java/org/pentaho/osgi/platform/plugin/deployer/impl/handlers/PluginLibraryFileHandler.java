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

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import com.google.common.io.ByteStreams;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
  public static final String LIB = "/lib/";

  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.contains( LIB ) && fileName.endsWith( JAR );
  }

  @Override public boolean handle( String relativePath, byte[] file, PluginMetadata pluginMetadata )
    throws PluginHandlingException {
    //    pluginMetadata.getManifestUpdater().getClasspathEntries().add( relativePath );
    try ( ByteArrayInputStream fin = new ByteArrayInputStream( file );
          JarInputStream jarInputStream = new JarInputStream( fin ); ) {


      Object bundleSymbolicName = jarInputStream.getManifest().getMainAttributes().getValue( "Bundle-SymbolicName" );
      if ( bundleSymbolicName != null ) {
        // don't load a jar that is already provided as a bundle in the system.
        // If it has a Bundle-SymbolicName, it's assumed that we provide it already in the OSGi container
        log.info( String.format(
          "Jar identified as an OSGi bundle [%s]; no auto-deploy. Assumed to be provided by the container.",
          bundleSymbolicName.toString() ) );
        return false;
      }


      ZipEntry nextEntry;
      while ( ( nextEntry = jarInputStream.getNextEntry() ) != null ) {
        if ( nextEntry.isDirectory() ) {
          continue;
        }
        String name = nextEntry.getName();
        // Get the contents
        try ( ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
          (int) Math.min( Integer.MAX_VALUE, Math.max( 0, nextEntry.getSize() ) ) ) ) {
          ByteStreams.copy( jarInputStream, byteArrayOutputStream );

          if ( name.matches( ".+\\.xml" ) ) {
            String contents = byteArrayOutputStream.toString( "UTF-8" );
            if ( contents.contains( "http://www.springframework.org/schema/beans" ) ) {
              // It is a spring file. move it to SpringDM location
              FileWriter fileWriter = pluginMetadata.getFileWriter( "META-INF/spring/" + name );
              fileWriter.append( contents );
              fileWriter.close();
            }

          } else {

            OutputStream fileOut = pluginMetadata.getFileOutputStream( nextEntry.getName() );
            fileOut.write( byteArrayOutputStream.toByteArray() );
            fileOut.close();

          }

        }
      }

    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return false;
  }
}
