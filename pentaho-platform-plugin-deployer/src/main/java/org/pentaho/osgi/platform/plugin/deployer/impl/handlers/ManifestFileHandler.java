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
package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.util.regex.Pattern;

/**
 * Created by nbaker on 7/21/16.
 */
public class ManifestFileHandler implements PluginFileHandler {

  public static final Pattern LIB_PATTERN = Pattern.compile( ".+\\/lib\\/.+\\.jar"  );
  public static final String JAR = ".jar";
  public static final String LIB = "/lib/";

  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.contains( LIB ) && fileName.endsWith( JAR );
  }

  @Override public boolean handle( String relativePath, byte[] file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
//    pluginMetadata.getManifestUpdater().getClasspathEntries().add( relativePath );
    return false;
  }
}
