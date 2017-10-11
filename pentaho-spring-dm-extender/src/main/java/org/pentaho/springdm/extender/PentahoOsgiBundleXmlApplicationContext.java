/*!
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
 * Copyright (c) 2016 - 2017 Hitachi Vantara..  All rights reserved.
 */

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
