package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Created by nbaker on 8/19/16.
 */
public class ManifestHandler implements PluginFileHandler {

  public static final String MANIFEST = "META-INF/MANIFEST.MF";

  @Override public boolean handles( String fileName ) {
    return fileName.endsWith( MANIFEST );
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
    try {
      Manifest manifest = new Manifest( new FileInputStream( file ) );
      Attributes mainAttributes = manifest.getMainAttributes();
      for ( Map.Entry<Object, Object> entry : mainAttributes.entrySet() ) {
        switch ( entry.getKey().toString() ){
          case "Export-Service":
          case "Import-Service":
          case "Require-Capability":
          case "Export-Package":
            pluginMetadata.getManifestUpdater().addEntry( entry.getKey(), entry.getValue() );
            break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}