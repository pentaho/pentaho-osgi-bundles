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

package org.pentaho.osgi.platform.webjars;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by nbaker on 9/5/14.
 */
public class PentahoWebjarsTransformer implements ArtifactUrlTransformer {
  private Logger logger = LoggerFactory.getLogger( PentahoWebjarsTransformer.class );

  @Override public URL transform( URL url ) throws Exception {
    return new URL( "pentaho-webjars", null, url.toExternalForm() );
  }

  @Override public boolean canHandle( File file ) {

    if ( file == null || file.getName() == null || !file.getName().endsWith( ".jar" ) ) {
      return false;
    }
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile( file );
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while ( entries.hasMoreElements() ) {
        // META-INF/resources/webjars/angularjs/1.3.0-rc.0/webjars-requirejs.js
        String name = entries.nextElement().getName();
        if ( name.endsWith( "-requirejs.js" ) ) {
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
}
