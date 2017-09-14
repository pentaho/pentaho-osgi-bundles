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

package org.pentaho.osgi.platform.plugin.deployer;

import org.apache.karaf.util.DeployerUtils;
import org.apache.karaf.util.maven.Parser;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.BundleStateManager;
import org.pentaho.osgi.platform.plugin.deployer.impl.PluginZipFileProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.jar.JarOutputStream;
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
  private BundleStateManager bundleStateManager;

  private static ExecutorService executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
    @Override
    public Thread newThread( Runnable r ) {
      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "PlatformPluginBundlingURLConnection pool" );
      return thread;
    }
  } );

  public PlatformPluginBundlingURLConnection( URL u, List<PluginFileHandler> pluginFileHandlers ) {
    this( u, pluginFileHandlers, null );
  }

  public PlatformPluginBundlingURLConnection( URL u, List<PluginFileHandler> pluginFileHandlers, BundleStateManager bundleStateManager ) {
    super( u );
    this.pluginFileHandlers = pluginFileHandlers;
    this.bundleStateManager = bundleStateManager;

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

  @Override
  public void connect() throws IOException {
    //Noop
  }

  @Override
  public InputStream getInputStream() throws IOException {
    final ExceptionPipedInputStream pipedInputStream =
      new ExceptionPipedInputStream( getMaxSize( getURL().getQuery() ) );


    String path = getURL().toString();
    String artifactName;
    String[] nameVersion;
    if ( path.startsWith( "file:" ) ) {
      artifactName = path.substring( path.lastIndexOf( "/" ) + 1 );
      nameVersion = new String[] {
        artifactName.substring( 0,
          artifactName.lastIndexOf( "." ) == -1 ? artifactName.length() : artifactName.lastIndexOf( "." ) ),
        "0.0.0"
      };

    } else {
      Parser parser = new Parser( path );
      String mvnPath = parser.getArtifactPath();
      int lastSlash = mvnPath.lastIndexOf( '/' );
      if ( lastSlash >= 0 ) {
        mvnPath = mvnPath.substring( lastSlash + 1 );
      }
      nameVersion = DeployerUtils.extractNameVersionType( mvnPath );
    }

    //Check to see if the bundle is already installed
    boolean isPluginProcessedBefore = bundleStateManager.isBundleInstalled( nameVersion[0] + nameVersion[1] );

    final PipedOutputStream pipedOutputStream = new PipedOutputStream( pipedInputStream );
    final ZipOutputStream zipOutputStream = new JarOutputStream( pipedOutputStream );
    final PluginZipFileProcessor pluginZipFileProcessor =
      new PluginZipFileProcessor( pluginFileHandlers, isPluginProcessedBefore, nameVersion[ 0 ],
        nameVersion[ 0 ], nameVersion[ 1 ] );
    pluginZipFileProcessor.processBackground( executorService, () -> {
        try {
          URLConnection connection = getURL().openConnection();
          InputStream connectionInputStream = connection.getInputStream();
          ZipInputStream zipInputStream = new ZipInputStream( connectionInputStream );
          return zipInputStream;
        } catch ( IOException ioe ) {
          throw new RuntimeException( ioe );
        }
      }, zipOutputStream,
      pipedInputStream );
    return pipedInputStream;

  }
}
