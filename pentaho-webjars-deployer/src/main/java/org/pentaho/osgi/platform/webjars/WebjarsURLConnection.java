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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;


import org.apache.commons.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Created by nbaker on 9/6/14.
 */
public class WebjarsURLConnection extends URLConnection {

  public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = Executors.defaultThreadFactory().newThread(r);
      thread.setDaemon(true);
      thread.setName("WebjarsURLConnection pool");
      return thread;
    }
  });

  public Future<Void> transform_thread;
  
  private static final String DEBUG_MESSAGE_FAILED_WRITING = "Problem transfering Jar content, probably JarOutputStream was already closed.";
  public static final String MANIFEST_MF = "MANIFEST.MF";
  public static final String PENTAHO_RJS_LOCATION = "META-INF/js/require.json";
  public static final String WEBJARS_REQUIREJS_NAME = "webjars-requirejs.js";
  private Logger logger = LoggerFactory.getLogger( getClass() );
  public static final Pattern MODULE_PATTERN = Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + WEBJARS_REQUIREJS_NAME );

  public WebjarsURLConnection( URL url ) {
    super( url );
  }

  @Override public void connect() throws IOException {

  }

  @Override public InputStream getInputStream() throws IOException {
    try {

      final PipedOutputStream pipedOutputStream = new PipedOutputStream();
      PipedInputStream pipedInputStream = new PipedInputStream( pipedOutputStream );

      transform_thread = EXECUTOR.submit( new Callable<Void>() {
        @Override public Void call() throws Exception {
          try {
            transform( getURL(), pipedOutputStream );
          } catch ( Exception e ) {
            logger.error( "Error Transforming zip", e );
            pipedOutputStream.close();
            throw e;
          }
          return null;
        }
      } );


      return pipedInputStream;

    } catch ( Exception e ) {
      logger.error( "Error opening Spring xml url", e );
      throw (IOException) new IOException( "Error opening Spring xml url" ).initCause( e );
    }

  }

  private void transform( URL url, PipedOutputStream pipedOutputStream ) throws IOException {

    String artifactName = "unknown";
    Version version = new Version( 0, 0, 0 );
    if ( url.getProtocol().equals( "file" ) ) {
      String filePath = url.getFile();
      int start = filePath.lastIndexOf( '/' );
      if(start >= 0) {
        artifactName = filePath.substring( filePath.lastIndexOf( '/' ) + 1, filePath.length() );  
      } else {
        artifactName = filePath;
      }
    } else if ( url.getProtocol().equals( "mvn" ) ) {
      String[] parts = url.getPath().split( "/" );
      artifactName = parts[ 1 ];
      String versionPart = parts[ 2 ];
      // version needs to be coerced into OSGI form Major.Minor.Patch.Classifier
      version = VersionParser.parseVersion( versionPart );
    }

    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

    Manifest manifest = jarInputStream.getManifest();
    if( manifest == null ){
      manifest = new Manifest();
    }
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webjars-" + artifactName );
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.IMPORT_PACKAGE ),
            "org.osgi.service.http,org.apache.felix.http.api,org.ops4j.pax.web.extender.whiteboard.runtime," +
                "org.ops4j.pax.web.extender.whiteboard" );

    manifest.getMainAttributes().put( new Attributes.Name( Constants.BUNDLE_VERSION ), version.toString() );
    
    try {
      JarOutputStream jarOutputStream = new JarOutputStream( pipedOutputStream, manifest );

      ZipEntry entry;
      String moduleName = "unknown";
      String moduleVersion = "unknown";
      boolean foundRJs = false;

      while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
        String name = entry.getName();
        if ( name.endsWith( MANIFEST_MF ) ) {
          // ignore existing manifest, we'll update it after the copy
          logger.info( "skipping manifest" );
        } else if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
          Matcher matcher = MODULE_PATTERN.matcher( name );
          if ( matcher.matches() == false ) {
            logger.error( "Webjars structure isn't right" );
            continue;
          }
          foundRJs = true;
          logger.info( "found WEBJARS config" );
          moduleName = matcher.group( 1 );
          moduleVersion = matcher.group( 2 );

          byte[] bytes = IOUtils.toByteArray( jarInputStream );
          String webjarsConfig = new String( bytes, "UTF-8" );
          String convertedConfig = convertConfig( webjarsConfig, moduleName, moduleVersion );

          ZipEntry newEntry = new ZipEntry( "META-INF/js/require.json" );
          jarOutputStream.putNextEntry( newEntry );
          jarOutputStream.write( convertedConfig.getBytes( "UTF-8" ) );
          // Process webjars into our form
          jarOutputStream.closeEntry();
        } else {
          try {
            logger.info( "copying misc entry: " + name );
            jarOutputStream.putNextEntry( entry );
            IOUtils.copy( jarInputStream, jarOutputStream );
            jarOutputStream.closeEntry();
          } catch ( IOException ioexception ) {
            logger.debug( DEBUG_MESSAGE_FAILED_WRITING, ioexception );
            //throw ioexception;
            return;
          }
        }
      }
      // Add Blueprint file if we found a require-js configuration.
      if( foundRJs ) {
        ZipEntry newEntry = new ZipEntry( "OSGI-INF/blueprint/blueprint.xml" );
        String blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream(
            "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );
        blueprintTemplate = blueprintTemplate.replaceAll( "\\{path\\}",
            "META-INF/resources/webjars/" + moduleName + "/" + moduleVersion );
        blueprintTemplate = blueprintTemplate.replace( "{versioned_name}", moduleName + "/" + moduleVersion );
        blueprintTemplate = blueprintTemplate.replace( "{name}", moduleName );
        jarOutputStream.putNextEntry( newEntry );
        jarOutputStream.write( blueprintTemplate.getBytes( "UTF-8" ) );
      }
      // Process webjars into our form
      try {
        jarOutputStream.closeEntry();
        pipedOutputStream.flush();
        jarOutputStream.close();
      } catch ( IOException ioexception ) {
        logger.debug( DEBUG_MESSAGE_FAILED_WRITING, ioexception );
        return;
      }
    } finally {
      logger.debug( "Closing JarInputStream." );
      try {
        jarInputStream.close();
      } catch ( IOException ioexception ) {
        logger.debug( "Tried to close JarInputStream, but it was already closed.", ioexception );
      }
    }
  }

  private String convertConfig( String config, String moduleName, String moduleVersion ) {

    Pattern pat = Pattern.compile( "webjars!(.*).js" );
    Matcher m = pat.matcher( config );

    StringBuffer sb = new StringBuffer();
    while ( m.find() ) {
      m.appendReplacement( sb, m.group( 1 ) );
    }
    m.appendTail( sb );

    config = sb.toString();

    pat = Pattern.compile( "webjars\\.path\\(['\"]{1}(.*)['\"]{1}, ['\"]{1}(.*)['\"]{1}\\)" );
    m = pat.matcher( config );
    while ( m.find() ) {
      m.appendReplacement( sb, "\"" + moduleName + "/" + moduleVersion + "/" + m.group( 2 ) + "\"" );
    }
    m.appendTail( sb );

    config = sb.toString();


    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName( "JavaScript" );
    try {
      String script = null;
      try {
        script = IOUtils.toString(
            getClass().getResourceAsStream( "/org/pentaho/osgi/platform/webjars/require-js-aggregator.js" ) );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      script = script.replace( "{{EXTERNAL_CONFIG}}", config );

      //      Context.enter().getWrapFactory().setJavaPrimitiveWrap( false );
      engine.eval( script );

      return ( (Invocable) engine ).invokeFunction( "processConfig", "" ).toString();

    } catch ( ScriptException e ) {
      return e.getMessage();
    } catch ( NoSuchMethodException e ) {
      e.printStackTrace();
    }
    return null;

  }

}
