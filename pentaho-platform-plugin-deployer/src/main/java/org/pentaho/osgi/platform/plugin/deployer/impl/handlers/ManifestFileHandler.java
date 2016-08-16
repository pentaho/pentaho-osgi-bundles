package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by nbaker on 7/21/16.
 */
public class ManifestFileHandler implements PluginFileHandler {

  public static final Pattern LIB_PATTERN = Pattern.compile( ".+\\/lib\\/.+\\.jar"  );
  public static final String JAR = ".jar";

  @Override public boolean handles( String fileName ) {
    return LIB_PATTERN.matcher( fileName ).matches();
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
//    pluginMetadata.getManifestUpdater().getClasspathEntries().add( relativePath );
  }
}
