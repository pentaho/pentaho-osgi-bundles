/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.springdm.extender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created by nbaker on 7/20/16.
 */
public class PentahoOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {

  private static final Logger log;

  public PentahoOsgiBundleXmlApplicationContext( String[] configurationLocations ) {
    super( configurationLocations );
  }

  @Override public Resource getResource( String location ) {

    int index = location.indexOf( "plugin:" );
    if ( index != 0 ) {
      return super.getResource( location );
    }

    location = location.substring( "plugin:".length() );
    String pluginPath = getBundle().getSymbolicName() + "/";

    Resource resourceByPath = super.getResource( "osgibundlejar:/" + pluginPath + location );
    if ( resourceByPath != null ) {
      return resourceByPath;
    }
    return null;
  }

  @Override protected Resource getResourceByPath( String path ) {
    int index = path.indexOf( "plugin:" );
    if ( index != 0 ) {
      return super.getResourceByPath( path );
    }

    path = path.substring( "plugin:".length() );
    String pluginPath = "";
    try {
      Enumeration<URL> resources = getBundle().getResources( "/" );
      while ( resources.hasMoreElements() ) {
        URL url = resources.nextElement();
        if ( !url.getPath().endsWith( "-INF" ) ) {
          pluginPath = url.getPath();
          break;
        }
      }
    } catch ( IOException e ) {
      log.error( "Error getting resources by path", e );
    }

    Resource resourceByPath = super.getResourceByPath( "classpath:" + pluginPath + "/" + path );
    if ( resourceByPath != null ) {
      return resourceByPath;
    }
    return null;
  }

  static {
    log = LoggerFactory.getLogger( PentahoOsgiBundleXmlApplicationContext.class );
  }

}
