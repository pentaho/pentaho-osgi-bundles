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
 * Copyright 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.webpackage.deployer.archive.impl;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UrlTransformer implements ArtifactUrlTransformer {
  private Logger logger = LoggerFactory.getLogger( UrlTransformer.class );

  @Override
  public URL transform( URL url ) throws Exception {
    return new URL( WebPackageURLConnection.URL_PROTOCOL, null, url.toExternalForm() );
  }

  @Override
  public boolean canHandle( File file ) {
    if ( !file.exists() ) {
      return false;
    }

    if ( this.isTarGzFile( file ) ) {
      return canHandleTarGzFile( file );
    } else if ( this.isZipFile( file ) ) {
      return canHandleZipFile( file );
    }

    return false;
  }

  boolean canHandleZipFile( File file ) {
    ZipFile zipFile = null;

    try {
      zipFile = new ZipFile( file );

      // exclude real jar files
      // (we only accept the jar extension because of exploded bundles (jardir))
      if ( zipFile.getEntry( "META-INF/MANIFEST.MF" ) != null ) {
        return false;
      }

      ZipInputStream zipInputStream = null;

      try {
        zipInputStream = new ZipInputStream( new FileInputStream( file ) );

        ZipEntry entry;
        while ( ( entry = zipInputStream.getNextEntry() ) != null ) {
          final String name = FilenameUtils.getName( entry.getName() );
          if ( name.equals( WebPackageURLConnection.PACKAGE_JSON ) ) {
            return true;
          }
        }
      } catch ( IOException ignored ) {
        // Ignore
      } finally {
        try {
          if ( zipInputStream != null ) {
            zipInputStream.close();
          }
        } catch ( IOException ignored ) {
          // Ignore
        }
      }
    } catch ( IOException ignored ) {
      // Ignore
    } finally {
      if ( zipFile != null ) {
        try {
          zipFile.close();
        } catch ( IOException ignored ) {
          // Ignore
        }
      }
    }

    return false;
  }

  boolean canHandleTarGzFile( File file ) {
    TarArchiveInputStream tarInput = null;
    try {
      tarInput = new TarArchiveInputStream( new GzipCompressorInputStream( new FileInputStream( file ) ) );
      TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
      while ( currentEntry != null ) {
        if ( currentEntry.getName().endsWith( WebPackageURLConnection.PACKAGE_JSON ) ) {
          return true;
        }

        currentEntry = tarInput.getNextTarEntry();
      }
    } catch ( IOException ignored ) {
      // Ignore
    } finally {
      if ( tarInput != null ) {
        try {
          tarInput.close();
        } catch ( IOException ignored ) {
          // Ignore
        }
      }
    }

    return false;
  }

  boolean isZipFile( File file ) {
    return file.getName().endsWith( ".zip" ) || file.getName().endsWith( ".jar" );
  }

  boolean isTarGzFile( File file ) {
    return file.getName().endsWith( ".tgz" ) || file.getName().endsWith( ".tar.gz" );
  }
}
