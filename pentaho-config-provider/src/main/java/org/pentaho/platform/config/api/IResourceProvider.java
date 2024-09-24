package org.pentaho.platform.config.api;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by nbaker on 8/8/15.
 */
public interface IResourceProvider {
  InputStream resolveResource( String resourcePath ) throws FileNotFoundException;
  boolean isInheriting();
}
