package org.pentaho.springdm.extender;

import org.springframework.core.io.Resource;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created by nbaker on 7/20/16.
 */
public class PentahoOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {
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
        if( !url.getPath().endsWith("-INF") ){
          pluginPath = url.getPath();
          break;
        }
      }
    } catch ( IOException e ) {
      logger.error( e );
    }

    Resource resourceByPath = super.getResourceByPath( "classpath:" + pluginPath + "/" + path );
    if ( resourceByPath != null ) {
      return resourceByPath;
    }
    return null;
  }
}
