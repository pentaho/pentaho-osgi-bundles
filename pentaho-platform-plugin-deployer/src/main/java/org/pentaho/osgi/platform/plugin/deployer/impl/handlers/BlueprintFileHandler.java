/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2017 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

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
  public static final String XML = ".xml";
  public static final String OSGI_INF_BLUEPRINT = "OSGI-INF/blueprint/";

  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.contains( OSGI_INF_BLUEPRINT ) && fileName.endsWith( XML );
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
    try ( FileReader fileReader = new FileReader( file );
          FileWriter fileWriter = pluginMetadata.getFileWriter( relativePath.substring( relativePath.indexOf( "/" ) + 1 ) ) ) {
      int read;
      while ( ( read = fileReader.read() ) != -1 ) {
        fileWriter.write( read );
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}

