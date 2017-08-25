/*
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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

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
