package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by nbaker on 7/21/16.
 */
public class BlueprintFileHandler implements PluginFileHandler {

  public static final Pattern LIB_PATTERN = Pattern.compile( ".+\\/OSGI-INF\\/blueprint\\/.*\\.xml" );
  public static final String JAR = ".jar";

  @Override public boolean handles( String fileName ) {
    return LIB_PATTERN.matcher( fileName ).matches();
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
    try ( FileReader fileReader = new FileReader( file );
          FileWriter fileWriter = pluginMetadata.getFileWriter( relativePath.substring( relativePath.indexOf( "/" ) + 1 ) ); ) {
      int read;
      while ( ( read = fileReader.read() ) != -1 ) {
        fileWriter.write( read );
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}

