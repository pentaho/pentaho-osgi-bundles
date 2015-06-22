/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer;

import org.apache.karaf.util.DeployerUtils;
import org.apache.karaf.util.maven.Parser;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.PluginZipFileProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Created by bryan on 8/27/14.
 */
public class PlatformPluginBundlingURLConnection extends URLConnection {
  public static final int TEN_MEGABYTES = 10 * 1024 * 1024;
  private static final Pattern maxSizePattern = Pattern.compile( "maxSize=([0-9]+)" );
  private final List<PluginFileHandler> pluginFileHandlers;

  public PlatformPluginBundlingURLConnection( URL u, List<PluginFileHandler> pluginFileHandlers ) {
    super( u );
    this.pluginFileHandlers = pluginFileHandlers;
  }

  public static int getMaxSize( String query ) {
    if ( query != null ) {
      Matcher matcher = maxSizePattern.matcher( query );
      if ( matcher.matches() ) {
        return Integer.parseInt( matcher.group( 1 ) );
      }
    }
    return TEN_MEGABYTES;
  }

  @Override public void connect() throws IOException {
    //Noop
  }

  @Override public InputStream getInputStream() throws IOException {
    final ExceptionPipedInputStream pipedInputStream =
      new ExceptionPipedInputStream( getMaxSize( getURL().getQuery() ) );
    Parser parser = new Parser(getURL().toString());
    String mvnPath = parser.getArtifactPath();
    int lastSlash = mvnPath.lastIndexOf( '/' );
    if ( lastSlash >= 0 ) {
      mvnPath = mvnPath.substring( lastSlash + 1 );
    }
    final String[] nameVersion = DeployerUtils.extractNameVersionType( mvnPath );
    final PipedOutputStream pipedOutputStream = new PipedOutputStream( pipedInputStream );
    final ZipOutputStream zipOutputStream = new ZipOutputStream( pipedOutputStream );
    URLConnection connection = getURL().openConnection();
    InputStream connectionInputStream = connection.getInputStream();
    ZipInputStream zipInputStream = new ZipInputStream( connectionInputStream );
    final PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, nameVersion[ 0 ], nameVersion[ 0 ], nameVersion[ 1 ] );
    pluginZipFileProcessor.processBackground( Executors.newSingleThreadExecutor(), zipInputStream, zipOutputStream,
      pipedInputStream );
    return pipedInputStream;
  }
}
