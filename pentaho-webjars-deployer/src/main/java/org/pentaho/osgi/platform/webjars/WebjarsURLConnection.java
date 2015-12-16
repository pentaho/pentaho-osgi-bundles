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
import org.pentaho.js.require.RequireJsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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

  public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool( 5, new ThreadFactory() {
    @SuppressWarnings( "NullableProblems" )
    @Override
    public Thread newThread( Runnable r ) {
      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "WebjarsURLConnection pool" );
      return thread;
    }
  } );

  public Future<Void> transform_thread;

  private static final String DEBUG_MESSAGE_FAILED_WRITING =
      "Problem transfering Jar content, probably JarOutputStream was already closed.";

  private static final String MANIFEST_MF = "MANIFEST.MF";
  private static final String PENTAHO_RJS_LOCATION = "META-INF/js/require.json";

  private static final String WEBJARS_REQUIREJS_NAME = "webjars-requirejs.js";
  private static final Pattern MODULE_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + WEBJARS_REQUIREJS_NAME );

  private static final String POM_NAME = "pom.xml";
  private static final Pattern POM_PATTERN = Pattern.compile( "META-INF/maven/org.webjars([^/]*)/([^/]+)/" + POM_NAME );

  private static final String BOWER_NAME = "bower.json";
  private static final Pattern BOWER_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + BOWER_NAME );

  private static final String NPM_NAME = "package.json";
  private static final Pattern NPM_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + NPM_NAME );

  private static final Pattern PACKAGE_FILES_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/.*" );

  private static final Pattern PACKAGE_JS_FILES_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/.*\\.js" );

  private static final ArrayList<String> JS_KNOWN_GLOBALS;

  static {
    JS_KNOWN_GLOBALS = new ArrayList<>();
    JS_KNOWN_GLOBALS.add( "applicationCache" );
    JS_KNOWN_GLOBALS.add( "caches" );
    JS_KNOWN_GLOBALS.add( "closed" );
    JS_KNOWN_GLOBALS.add( "Components" );
    JS_KNOWN_GLOBALS.add( "console" );
    JS_KNOWN_GLOBALS.add( "content" );
    JS_KNOWN_GLOBALS.add( "_content" );
    JS_KNOWN_GLOBALS.add( "controllers" );
    JS_KNOWN_GLOBALS.add( "crypto" );
    JS_KNOWN_GLOBALS.add( "defaultStatus" );
    JS_KNOWN_GLOBALS.add( "devicePixelRatio" );
    JS_KNOWN_GLOBALS.add( "dialogArguments" );
    JS_KNOWN_GLOBALS.add( "directories" );
    JS_KNOWN_GLOBALS.add( "document" );
    JS_KNOWN_GLOBALS.add( "frameElement" );
    JS_KNOWN_GLOBALS.add( "frames" );
    JS_KNOWN_GLOBALS.add( "fullScreen" );
    JS_KNOWN_GLOBALS.add( "globalStorage" );
    JS_KNOWN_GLOBALS.add( "history" );
    JS_KNOWN_GLOBALS.add( "innerHeight" );
    JS_KNOWN_GLOBALS.add( "innerWidth" );
    JS_KNOWN_GLOBALS.add( "length" );
    JS_KNOWN_GLOBALS.add( "location" );
    JS_KNOWN_GLOBALS.add( "locationbar" );
    JS_KNOWN_GLOBALS.add( "localStorage" );
    JS_KNOWN_GLOBALS.add( "menubar" );
    JS_KNOWN_GLOBALS.add( "messageManager" );
    JS_KNOWN_GLOBALS.add( "name" );
    JS_KNOWN_GLOBALS.add( "navigator" );
    JS_KNOWN_GLOBALS.add( "opener" );
    JS_KNOWN_GLOBALS.add( "outerHeight" );
    JS_KNOWN_GLOBALS.add( "outerWidth" );
    JS_KNOWN_GLOBALS.add( "pageXOffset" );
    JS_KNOWN_GLOBALS.add( "pageYOffset" );
    JS_KNOWN_GLOBALS.add( "sessionStorage" );
    JS_KNOWN_GLOBALS.add( "parent" );
    JS_KNOWN_GLOBALS.add( "performance" );
    JS_KNOWN_GLOBALS.add( "personalbar" );
    JS_KNOWN_GLOBALS.add( "pkcs11" );
    JS_KNOWN_GLOBALS.add( "returnValue" );
    JS_KNOWN_GLOBALS.add( "screen" );
    JS_KNOWN_GLOBALS.add( "screenX" );
    JS_KNOWN_GLOBALS.add( "screenY" );
    JS_KNOWN_GLOBALS.add( "scrollbars" );
    JS_KNOWN_GLOBALS.add( "scrollMaxX" );
    JS_KNOWN_GLOBALS.add( "scrollMaxY" );
    JS_KNOWN_GLOBALS.add( "scrollX" );
    JS_KNOWN_GLOBALS.add( "scrollY" );
    JS_KNOWN_GLOBALS.add( "self" );
    JS_KNOWN_GLOBALS.add( "sessionStorage" );
    JS_KNOWN_GLOBALS.add( "sidebar" );
    JS_KNOWN_GLOBALS.add( "status" );
    JS_KNOWN_GLOBALS.add( "statusbar" );
    JS_KNOWN_GLOBALS.add( "toolbar" );
    JS_KNOWN_GLOBALS.add( "top" );
    JS_KNOWN_GLOBALS.add( "window" );
  }

  private Logger logger = LoggerFactory.getLogger( getClass() );

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
    RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator.ArtifactInfo( url );

    final boolean isClassicWebjar = artifactInfo.getGroup().equals( "org.webjars" );
    boolean wasReadFromPom = false;

    final boolean isNpmWebjar = artifactInfo.getGroup().equals( "org.webjars.npm" );
    final boolean isBowerWebjar = artifactInfo.getGroup().equals( "org.webjars.bower" );

    ArrayList<String> exportedGlobals = new ArrayList<>();
    final boolean isAmdPackage = findAmdDefine( url, exportedGlobals );

    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();

    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );
    try {

      JarOutputStream jarOutputStream =
          new JarOutputStream( pipedOutputStream, getManifest( artifactInfo, jarInputStream ) );

      RequireJsGenerator requireConfig = null;

      String physicalPathNamePart = null;
      String physicalPathVersionPart = null;

      ZipEntry entry;
      while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
        String name = entry.getName();
        if ( name.endsWith( MANIFEST_MF ) ) {
          // ignore existing manifest, we've already created our own
          continue;
        }

        if ( requireConfig == null || isClassicWebjar && !wasReadFromPom ) {
          if ( isClassicWebjar ) {
            if ( name.endsWith( POM_NAME ) ) {
              // handcrafted requirejs configuration on pom.xml has top prioriy (Classic WebJars)
              Matcher matcher = POM_PATTERN.matcher( name );
              if ( matcher.matches() ) {
                try {
                  requireConfig = RequireJsGenerator.parsePom( jarInputStream );
                  wasReadFromPom = true;
                } catch ( Exception ignored ) {
                  // ignored
                }

                continue;
              }
            } else if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
              // next comes the module author's webjars-requirejs.js
              Matcher matcher = MODULE_PATTERN.matcher( name );
              if ( matcher.matches() ) {
                try {
                  requireConfig =
                      RequireJsGenerator.processJsScript( matcher.group( 1 ), matcher.group( 2 ), jarInputStream );
                } catch ( Exception ignored ) {
                  // ignored
                }

                continue;
              }
            }
          } else if ( isNpmWebjar && name.endsWith( NPM_NAME ) || isBowerWebjar && name.endsWith( BOWER_NAME ) ) {
            // try to generate requirejs.json from package.json (Npm WebJars) or bower.json (Bower WebJars)
            Matcher matcher = isNpmWebjar ? NPM_PATTERN.matcher( name ) : BOWER_PATTERN.matcher( name );
            if ( matcher.matches() ) {
              try {
                requireConfig = RequireJsGenerator.parseJsonPackage( jarInputStream );
              } catch ( Exception ignored ) {
                // ignored
              }

              continue;
            }
          }
        }

        try {
          jarOutputStream.putNextEntry( entry );
          IOUtils.copy( jarInputStream, jarOutputStream );
          jarOutputStream.closeEntry();
        } catch ( IOException ioexception ) {
          logger.debug( DEBUG_MESSAGE_FAILED_WRITING, ioexception );
          //throw ioexception;
          return;
        }

        // store the path of the first file that fits de expected folder structure,
        // for physical path mapping and also fallback on malformed webjars
        if ( physicalPathNamePart == null ) {
          Matcher matcher = PACKAGE_FILES_PATTERN.matcher( name );

          if ( matcher.matches() ) {
            physicalPathNamePart = matcher.group( 1 );
            physicalPathVersionPart = matcher.group( 2 );
          }
        }
      }

      if ( requireConfig == null ) {
        // in last resort generate requirejs config by mapping the root path
        requireConfig = RequireJsGenerator.emptyGenerator( physicalPathNamePart, physicalPathVersionPart );

        logger.warn( "malformed webjar " + url.toString() + " deployed using root path mapping" );
      }

      if ( requireConfig != null ) {
        try {
          final String exports = !isAmdPackage && !exportedGlobals.isEmpty() ? exportedGlobals.get( 0 ) : null;
          final RequireJsGenerator.ModuleInfo moduleInfo =
              requireConfig.getConvertedConfig( artifactInfo, isAmdPackage, exports );

          addRequireJsToJar( moduleInfo.exportRequireJs(), jarOutputStream );

          // Add Blueprint file if we found a require-js configuration.
          ZipEntry newEntry = new ZipEntry( "OSGI-INF/blueprint/blueprint.xml" );
          String blueprintTemplate =
              IOUtils.toString( getClass().getResourceAsStream(
                  "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );
          blueprintTemplate =
              blueprintTemplate.replaceAll( "\\{path\\}", "META-INF/resources/webjars/" + physicalPathNamePart + "/"
                  + physicalPathVersionPart );
          blueprintTemplate = blueprintTemplate.replace( "{versioned_name}", moduleInfo.getVersionedPath() );

          jarOutputStream.putNextEntry( newEntry );
          jarOutputStream.write( blueprintTemplate.getBytes( "UTF-8" ) );
        } catch ( Exception e ) {
          logger.error( "error saving " + PENTAHO_RJS_LOCATION + " - " + e.getMessage() );
        }
      }

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

  private boolean findAmdDefine( URL url, ArrayList<String> exports ) throws IOException {
    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();

    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

    try {
      ZipEntry entry;
      while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
        String name = entry.getName();

        Matcher matcher = PACKAGE_JS_FILES_PATTERN.matcher( name );

        if ( matcher.matches() ) {
          if ( findAmdDefine( jarInputStream, exports ) ) {
            return true;
          }
        }
      }
      return false;

    } finally {
      logger.debug( "Closing JarInputStream." );
      try {
        jarInputStream.close();
      } catch ( IOException ioexception ) {
        logger.debug( "Tried to close JarInputStream, but it was already closed.", ioexception );
      }
    }
  }

  private boolean findAmdDefine( InputStream is, ArrayList<String> exports ) {
    final Pattern definePattern =
        Pattern.compile( "\bdefine\b(\\s*)\\(((\\s*)\"[^\"]+\"(\\s*),)?((\\s*)\\[((\\s*)\"[^\"]+\""
            + "(\\s*),?)+(\\s*)\\](\\s*),)?((\\s*)function)" );

    final Pattern globalPattern =
        Pattern.compile( "(\\bwindow\\b|\\bexports\\b)\\.(([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*)\\s*=\\s*[\\w${][^,;]+" );

    BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

    String line;
    try {
      while ( ( line = br.readLine() ) != null ) {
        Matcher matcher = definePattern.matcher( line );
        if ( matcher.find() ) {
          return true;
        }

        matcher = globalPattern.matcher( line );
        if ( matcher.find() ) {
          final String var = matcher.group( 2 );
          final String varSegment = var.split( "\\.", 2 )[0];
          if ( !varSegment.startsWith( "on" ) && !JS_KNOWN_GLOBALS.contains( varSegment ) && !exports.contains( var ) ) {
            exports.add( var );
          }
        }
      }
    } catch ( IOException ignored ) {
      // ignored
    }

    return false;
  }

  private Manifest getManifest( RequireJsGenerator.ArtifactInfo artifactInfo, JarInputStream jarInputStream ) {
    Manifest manifest = jarInputStream.getManifest();
    if ( manifest == null ) {
      manifest = new Manifest();
      manifest.getMainAttributes().put( Attributes.Name.MANIFEST_VERSION, "1.0" );
    }
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webjars-" + artifactInfo.getArtifactId() );
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.IMPORT_PACKAGE ),
            "org.osgi.service.http,org.apache.felix.http.api,org.ops4j.pax.web.extender.whiteboard.runtime,"
                + "org.ops4j.pax.web.extender.whiteboard" );

    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.BUNDLE_VERSION ), artifactInfo.getOsgiCompatibleVersion() );
    return manifest;
  }

  private void addRequireJsToJar( String config, JarOutputStream jarOutputStream ) throws IOException {
    ZipEntry newEntry = new ZipEntry( PENTAHO_RJS_LOCATION );
    jarOutputStream.putNextEntry( newEntry );
    jarOutputStream.write( config.getBytes( "UTF-8" ) );
    jarOutputStream.closeEntry();
  }
}
