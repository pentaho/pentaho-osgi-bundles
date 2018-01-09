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
