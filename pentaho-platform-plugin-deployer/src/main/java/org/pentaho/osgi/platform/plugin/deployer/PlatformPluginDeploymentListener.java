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

package org.pentaho.osgi.platform.plugin.deployer;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by bryan on 8/26/14.
 */
public class PlatformPluginDeploymentListener implements ArtifactUrlTransformer {
  public static final String PROTOCOL = "pentaho-platform-plugin-file";
  public static final String PLUGIN_XML_FILENAME = "plugin.xml";
  private Logger logger = LoggerFactory.getLogger( PlatformPluginDeploymentListener.class );
  private URLFactory urlFactory = new URLFactory() {
    @Override public URL create( String protocol, String file ) throws MalformedURLException {
      return new URL( protocol, null, file );
    }
  };

  // For unit tests only
  protected void setUrlFactory( URLFactory urlFactory ) {
    this.urlFactory = urlFactory;
  }

  @Override public URL transform( URL artifact ) throws Exception {
    return urlFactory.create( PROTOCOL, artifact.getFile() );
  }

  @Override public boolean canHandle( File artifact ) {
    if ( artifact == null || artifact.getName() == null || !artifact.getName().endsWith( ".zip" ) ) {
      return false;
    }
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile( artifact );
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while ( entries.hasMoreElements() ) {
        String[] splitName = entries.nextElement().getName().split( "/" );
        if ( splitName.length == 2 && PLUGIN_XML_FILENAME.equals( splitName[ 1 ] ) ) {
          return true;
        }
      }
    } catch ( IOException e ) {
      logger.error( e.getMessage(), e );
    } finally {
      if ( zipFile != null ) {
        try {
          zipFile.close();
        } catch ( IOException e ) {
          // Ignore
        }
      }
    }
    return false;
  }

  public static interface URLFactory {
    public URL create( String protocol, String file ) throws MalformedURLException;
  }
}
