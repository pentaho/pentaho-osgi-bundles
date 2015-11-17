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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Constants;
import org.pentaho.js.require.RequireJsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  private final JSONParser parser = new JSONParser();

  private static final String MANIFEST_MF = "MANIFEST.MF";
  private static final String PENTAHO_RJS_LOCATION = "META-INF/js/require.json";

  private static final String WEBJARS_REQUIREJS_NAME = "webjars-requirejs.js";
  private static final Pattern MODULE_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + WEBJARS_REQUIREJS_NAME );

  private static final String POM_NAME = "pom.xml";
  private static final Pattern POM_PATTERN =
      Pattern.compile( "META-INF/maven/org.webjars([^/]*)/([^/]+)/" + POM_NAME );

  private static final String BOWER_NAME = "bower.json";
  private static final Pattern BOWER_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + BOWER_NAME );

  private static final String NPM_NAME = "package.json";
  private static final Pattern NPM_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + NPM_NAME );

  private static final Pattern PACKAGE_FILES_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/.*" );

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

      EXECUTOR.submit( new Callable<Void>() {
        @Override public Void call() throws Exception {
          try {
            transform( getURL(), pipedOutputStream );
          } catch ( Exception e ) {
            logger.error( "Error Transforming zip", e );
            pipedOutputStream.close();
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

    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

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
                requireConfig = moduleFromPom( jarInputStream );
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
                requireConfig = moduleFromJsScript( matcher.group( 1 ), matcher.group( 2 ), jarInputStream );
              } catch ( Exception ignored ) {
                // ignored
              }

              continue;
            }
          }
        } else if ( isNpmWebjar && name.endsWith( NPM_NAME ) || isBowerWebjar && name.endsWith( BOWER_NAME ) ) {
          // try to generate requirejs.json from bower.json (Bower WebJars)
          Matcher matcher = isNpmWebjar ? NPM_PATTERN.matcher( name ) : BOWER_PATTERN.matcher( name );
          if ( matcher.matches() ) {
            try {
              requireConfig = moduleFromJsonPackage( jarInputStream );
            } catch ( Exception ignored ) {
              // ignored
            }

            continue;
          }
        }
      }

      jarOutputStream.putNextEntry( entry );
      IOUtils.copy( jarInputStream, jarOutputStream );
      jarOutputStream.closeEntry();

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
      requireConfig = moduleFromRootPath( physicalPathNamePart, physicalPathVersionPart );

      logger.warn( "malformed webjar " + url.toString() + " deployed using root path mapping" );
    }

    if ( requireConfig != null ) {
      try {
        final RequireJsGenerator.ModuleInfo moduleInfo = requireConfig.getConvertedConfig( artifactInfo );

        addRequireJsToJar( JSONObject.toJSONString( moduleInfo.getRequirejs() ), jarOutputStream );

        // Add Blueprint file if we found a require-js configuration.
        ZipEntry newEntry = new ZipEntry( "OSGI-INF/blueprint/blueprint.xml" );
        String blueprintTemplate = IOUtils
            .toString( getClass().getResourceAsStream( "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );
        blueprintTemplate =
            blueprintTemplate.replaceAll( "\\{path\\}",
                "META-INF/resources/webjars/" + physicalPathNamePart + "/" + physicalPathVersionPart );
        blueprintTemplate = blueprintTemplate.replace( "{versioned_name}", moduleInfo.getVersionedName() );

        jarOutputStream.putNextEntry( newEntry );
        jarOutputStream.write( blueprintTemplate.getBytes( "UTF-8" ) );
      } catch ( ParseException e ) {
        logger.error( "error saving " + PENTAHO_RJS_LOCATION + " - " + e.getMessage() );
      }
    }

    jarOutputStream.closeEntry();

    pipedOutputStream.flush();
    jarOutputStream.close();
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

    manifest.getMainAttributes().put( new Attributes.Name( Constants.BUNDLE_VERSION ), artifactInfo.getOsgiCompatibleVersion() );
    return manifest;
  }

  private RequireJsGenerator moduleFromPom( JarInputStream jarInputStream )
      throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, ParseException {
    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( bytes ) );
    return new RequireJsGenerator( pom );
  }

  private RequireJsGenerator moduleFromJsScript( String moduleName, String moduleVersion,
                                                 JarInputStream jarInputStream )
      throws IOException, NoSuchMethodException, ScriptException, ParseException {
    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    return new RequireJsGenerator( moduleName, moduleVersion, new String( bytes, "UTF-8" ) );
  }

  private RequireJsGenerator moduleFromJsonPackage( JarInputStream jarInputStream ) throws IOException, ParseException {
    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    String packageConfig = new String( bytes, "UTF-8" );

    JSONObject json = (JSONObject) parser.parse( packageConfig );
    return new RequireJsGenerator( json );
  }

  private RequireJsGenerator moduleFromRootPath( String physicalPathNamePart,
                                                 String physicalPathVersionPart ) {
    return new RequireJsGenerator( physicalPathNamePart, physicalPathVersionPart );
  }

  private void addRequireJsToJar( String config, JarOutputStream jarOutputStream ) throws IOException {
    ZipEntry newEntry = new ZipEntry( PENTAHO_RJS_LOCATION );
    jarOutputStream.putNextEntry( newEntry );
    jarOutputStream.write( config.getBytes( "UTF-8" ) );
    jarOutputStream.closeEntry();
  }
}
