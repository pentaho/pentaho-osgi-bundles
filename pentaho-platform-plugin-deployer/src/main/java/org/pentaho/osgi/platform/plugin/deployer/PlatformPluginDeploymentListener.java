/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
