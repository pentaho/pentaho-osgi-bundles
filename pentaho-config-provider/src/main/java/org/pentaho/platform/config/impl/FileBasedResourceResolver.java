package org.pentaho.platform.config.impl;

import org.pentaho.platform.config.api.IResourceProvider;
import org.pentaho.platform.config.api.IResourceResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by nbaker on 8/8/15.
 */
public class FileBasedResourceResolver implements IResourceProvider {
  private final File basePath;
  private final boolean inheriting;

  public FileBasedResourceResolver( File basePath, boolean inheriting ) {
    this.basePath = basePath;
    this.inheriting = inheriting;
  }

  @Override public InputStream resolveResource( String resourcePath ) throws FileNotFoundException {
    // ensure relative
    if( resourcePath.startsWith( "/" ) ){
      resourcePath = resourcePath.substring( 1 );
    }
    File file = new File( basePath, resourcePath );
    return new FileInputStream( file );
  }

  @Override public boolean isInheriting() {
    return inheriting;
  }
}
