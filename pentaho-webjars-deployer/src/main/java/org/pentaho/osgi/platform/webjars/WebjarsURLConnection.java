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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.WarningLevel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.osgi.framework.Constants;
import org.pentaho.js.require.RequireJsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

  private static final ArrayList<String> JS_KNOWN_GLOBALS;

  private static final int BYTES_BUFFER_SIZE = 4096;
  private static final String WEBJAR_SRC_ALIAS_PREFIX = "webjar-src";
  private static final String MINIFIED_RESOURCES_OUTPUT_PATH = "META-INF/resources/dist-gen";

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
      throw new IOException( "Error opening Spring xml url", e );
    }
  }

  private void transform( URL url, PipedOutputStream pipedOutputStream ) throws IOException {
    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();

    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

    Path absTempPath = Files.createTempDirectory( "WebjarsURLConnection" );

    try {
      RequireJsGenerator.ArtifactInfo artifactInfo = new RequireJsGenerator.ArtifactInfo( url );

      JarOutputStream jarOutputStream =
          new JarOutputStream( pipedOutputStream, getManifest( artifactInfo, jarInputStream ) );

      final boolean isClassicWebjar = artifactInfo.getGroup().equals( "org.webjars" );
      boolean wasReadFromPom = false;

      final boolean isNpmWebjar = artifactInfo.getGroup().equals( "org.webjars.npm" );
      final boolean isBowerWebjar = artifactInfo.getGroup().equals( "org.webjars.bower" );

      ArrayList<String> exportedGlobals = new ArrayList<>();
      boolean isAmdPackage = false;

      RequireJsGenerator requireConfig = null;

      String packageName = null;
      String packageVersion = null;

      // version read from pom file, used as fallback in bower webjars without version information
      String pomProjectVersion = null;

      String relSrcPath = null;
      Path absSrcPath = null;

      boolean minificationFailed = false;

      ZipEntry entry;
      while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
        String name = entry.getName();

        if ( name.endsWith( MANIFEST_MF ) ) {
          // ignore existing manifest, we already created our own
          jarInputStream.closeEntry();
          continue;
        }

        if ( name.endsWith( POM_NAME ) && POM_PATTERN.matcher( name ).matches() ) {
          if ( isClassicWebjar ) {
            // handcrafted requirejs configuration on pom.xml has top prioriy (Classic WebJars)
            try {
              requireConfig = RequireJsGenerator.parsePom( jarInputStream );
              wasReadFromPom = true;
            } catch ( Exception ignored ) {
              // ignored
            }
          } else if ( isBowerWebjar ) {
            // collect from the pom file just the version information, for fallback in bower webjars
            try {
              pomProjectVersion = RequireJsGenerator.getWebjarVersionFromPom( jarInputStream );

              // fill in the version if bower.json was already processed
              if ( requireConfig != null &&
                  requireConfig.getModuleInfo() != null &&
                  requireConfig.getModuleInfo().getVersion() == null ) {
                requireConfig.getModuleInfo().setVersion( pomProjectVersion );
              }
            } catch ( Exception ignored ) {
              // ignored
            }
          }

          // the pom file will not be copied to the generated bundle,
          // because it would be troublesome as the entry stream is already consumed
          // and it isn't really needed anyway
          continue;
        }

        boolean isPackageFile = false;

        // store the path of the first file that fits de expected folder structure,
        // for physical path mapping and also fallback on malformed webjars
        if ( packageName == null ) {
          Matcher matcher = PACKAGE_FILES_PATTERN.matcher( name );

          if ( matcher.matches() ) {
            packageName = matcher.group( 1 );
            packageVersion = matcher.group( 2 );

            relSrcPath = "META-INF/resources/webjars/" + packageName + File.separator + packageVersion;
            absSrcPath = absTempPath.resolve( relSrcPath );

            isPackageFile = true;
          }
        } else {
          isPackageFile = name.contains( packageName + File.separator + packageVersion );
        }

        if ( !entry.isDirectory() ) {
          File outputFile = null;
          BufferedOutputStream bos = null;

          if ( isPackageFile ) {
            outputFile = new File( absTempPath.toAbsolutePath() + File.separator + name );
            outputFile.getParentFile().mkdirs();

            bos = new BufferedOutputStream( new FileOutputStream( outputFile ) );
          }

          ZipEntry zipEntry = new ZipEntry( name );
          jarOutputStream.putNextEntry( zipEntry );

          byte[] bytes = new byte[ BYTES_BUFFER_SIZE ];
          int read;
          while ( ( read = jarInputStream.read( bytes ) ) != -1 ) {
            if ( isPackageFile ) {
              bos.write( bytes, 0, read );
            }

            jarOutputStream.write( bytes, 0, read );
          }

          jarOutputStream.closeEntry();

          if ( isPackageFile ) {
            bos.close();

            if ( !isAmdPackage && isJsFile( name ) && findAmdDefine( new FileInputStream( outputFile ),
                exportedGlobals ) ) {
              isAmdPackage = true;
            }
          }

          if ( requireConfig == null || ( isClassicWebjar && !wasReadFromPom ) ) {
            if ( isClassicWebjar ) {
              if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
                // next comes the module author's webjars-requirejs.js
                Matcher matcher = MODULE_PATTERN.matcher( name );
                if ( matcher.matches() ) {
                  try {
                    requireConfig = RequireJsGenerator.processJsScript(
                        matcher.group( 1 ), matcher.group( 2 ), new FileInputStream( outputFile ) );
                  } catch ( Exception ignored ) {
                    // ignored
                  }
                }
              }
            } else if ( isNpmWebjar && name.endsWith( NPM_NAME ) || isBowerWebjar && name.endsWith( BOWER_NAME ) ) {
              // try to generate requirejs.json from package.json (Npm WebJars) or bower.json (Bower WebJars)
              Matcher matcher = isNpmWebjar ? NPM_PATTERN.matcher( name ) : BOWER_PATTERN.matcher( name );
              if ( matcher.matches() ) {
                try {
                  requireConfig = RequireJsGenerator.parseJsonPackage( new FileInputStream( outputFile ) );

                  // on bower webjars, check if the version information is present
                  // if not fill it with the version extracted from pom, if already gathered
                  if ( isBowerWebjar &&
                      requireConfig != null && requireConfig.getModuleInfo() != null &&
                      requireConfig.getModuleInfo().getVersion() == null ) {
                    if ( pomProjectVersion != null ) {
                      requireConfig.getModuleInfo().setVersion( pomProjectVersion );
                    }
                  }
                } catch ( Exception ignored ) {
                  // ignored
                }
              }
            }
          }
        }

        jarInputStream.closeEntry();
      }

      Collection<File> scrFiles = FileUtils.listFiles(
          absSrcPath.toFile(),
          TrueFileFilter.INSTANCE,
          TrueFileFilter.INSTANCE
      );

      try {
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel( options );

        options.setSourceMapOutputPath( "." );

        WarningLevel.QUIET.setOptionsForWarningLevel( options );
        options.setWarningLevel( DiagnosticGroups.LINT_CHECKS, CheckLevel.OFF );

        SourceFile externs = SourceFile.fromCode( "externs.js", "" );

        String lastPrefix = null;
        SourceMap.LocationMapping lm = null;

        for ( File srcFile : scrFiles ) {
          final String name = srcFile.getName();

          if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
            continue;
          }

          final String relSrcFilePath = absSrcPath.relativize( srcFile.toPath() ).toString();
          final String relOutFilePath = MINIFIED_RESOURCES_OUTPUT_PATH + File.separator + relSrcFilePath;

          if ( isJsFile( name ) ) {
            // Check if there is a .map with the same name
            File mapFile = new File( name + ".map", srcFile.getParent() );
            if ( mapFile.exists() ) {
              addFileToZip( jarOutputStream, relOutFilePath, srcFile );
              addFileToZip( jarOutputStream, relOutFilePath + ".map", mapFile );
              continue;
            }

            final Path srcFileFolder = srcFile.toPath().getParent();
            final String prefix = srcFileFolder.toString();
            if ( lastPrefix == null || !lastPrefix.equals( prefix ) ) {
              String relPath = absSrcPath.relativize( srcFileFolder ).toString();
              if ( !relPath.isEmpty() ) {
                relPath = File.separator + relPath;
              }

              String reverseRelPath = srcFileFolder.relativize( absSrcPath ).toString();
              if ( !reverseRelPath.isEmpty() ) {
                reverseRelPath += File.separator;
              }

              final String replacement =
                  "../../" + reverseRelPath + WEBJAR_SRC_ALIAS_PREFIX + File.separator
                      + packageName + File.separator
                      + packageVersion + relPath;

              lm = new SourceMap.LocationMapping( prefix, replacement );

              lastPrefix = prefix;
            }

            List<SourceMap.LocationMapping> lms = new ArrayList<>();
            lms.add( lm );
            options.setSourceMapLocationMappings( lms );

            Compiler compiler = new Compiler();

            SourceFile input = SourceFile.fromFile( srcFile );
            input.setOriginalPath( relSrcFilePath );

            Result res = compiler.compile( externs, input, options );
            if ( res.success ) {
              StringBuilder code = new StringBuilder( compiler.toSource() );
              code.append( "\n//# sourceMappingURL=" ).append( name ).append( ".map" );

              StringBuilder sourcemap = new StringBuilder();
              SourceMap sm = compiler.getSourceMap();
              sm.appendTo( sourcemap, name );

              addFileToZip( jarOutputStream, relOutFilePath, code.toString() );
              addFileToZip( jarOutputStream, relOutFilePath + ".map", sourcemap.toString() );
            } else {
              addFileToZip( jarOutputStream, relOutFilePath, srcFile );

              logger.warn( "error minifing " + relSrcFilePath + ", copied original version" );
            }
          } else if ( !isMapFile( name ) ) {
            addFileToZip( jarOutputStream, relOutFilePath, srcFile );
          }
        }
      } catch ( Exception e ) {
        minificationFailed = true;

        logger.warn( "exception minifing " + url.toString() + ", serving original files", e );
      }

      if ( requireConfig == null ) {
        // in last resort generate requirejs config by mapping the root path
        requireConfig = RequireJsGenerator.emptyGenerator( packageName, packageVersion );

        logger.warn( "malformed webjar " + url.toString() + " deployed using root path mapping" );
      }

      if ( requireConfig != null ) {
        try {
          final String exports = !isAmdPackage && !exportedGlobals.isEmpty() ? exportedGlobals.get( 0 ) : null;
          final RequireJsGenerator.ModuleInfo moduleInfo =
              requireConfig.getConvertedConfig( artifactInfo, isAmdPackage, exports );

          addFileToZip( jarOutputStream, PENTAHO_RJS_LOCATION, moduleInfo.exportRequireJs() );

          String blueprintTemplate;
          if ( minificationFailed ) {
            blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream(
                "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );

            blueprintTemplate = blueprintTemplate.replace( "{dist_path}", relSrcPath );
            blueprintTemplate = blueprintTemplate.replace( "{dist_alias}", moduleInfo.getVersionedPath() );
          } else {
            blueprintTemplate =
                IOUtils.toString( getClass().getResourceAsStream(
                    "/org/pentaho/osgi/platform/webjars/blueprint-minified-template.xml" ) );

            blueprintTemplate = blueprintTemplate.replace( "{src_path}", relSrcPath );
            blueprintTemplate = blueprintTemplate
                .replace( "{src_alias}", WEBJAR_SRC_ALIAS_PREFIX + File.separator + moduleInfo.getVersionedPath() );

            blueprintTemplate = blueprintTemplate.replace( "{dist_path}", MINIFIED_RESOURCES_OUTPUT_PATH );
            blueprintTemplate = blueprintTemplate.replace( "{dist_alias}", moduleInfo.getVersionedPath() );
          }

          addFileToZip( jarOutputStream, "OSGI-INF/blueprint/blueprint.xml", blueprintTemplate );
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
      }
    } catch ( IOException e ) {
      logger.debug( url.toString() + ": Pipe is closed, no need to continue.", e );
    } finally {
      try {
        FileUtils.deleteDirectory( absTempPath.toFile() );
      } catch ( IOException ignored ) {
        // ignored
      }

      logger.debug( "Closing JarInputStream." );
      try {
        jarInputStream.close();
      } catch ( IOException ioexception ) {
        logger.debug( "Tried to close JarInputStream, but it was already closed.", ioexception );
      }
    }
  }

  private boolean isJsFile( String name ) {
    return name.endsWith( ".js" );
  }

  private boolean isMapFile( String name ) {
    return name.endsWith( ".map" );
  }

  private void addFileToZip( JarOutputStream zip, String entry, File file ) throws IOException {
    int bytesIn;
    byte[] readBuffer = new byte[ BYTES_BUFFER_SIZE ];

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream( file );

      ZipEntry zipEntry = new ZipEntry( entry );
      zip.putNextEntry( zipEntry );

      bytesIn = inputStream.read( readBuffer );
      while ( bytesIn != -1 ) {
        zip.write( readBuffer, 0, bytesIn );
        bytesIn = inputStream.read( readBuffer );
      }
    } finally {
      try {
        if ( inputStream != null ) {
          inputStream.close();
        }
      } catch ( IOException ignored ) {
        // ignored
      }
    }
  }

  private void addFileToZip( JarOutputStream zip, String entry, String content ) throws IOException {
    ZipEntry zipEntry = new ZipEntry( entry );
    zip.putNextEntry( zipEntry );
    zip.write( content.getBytes( "UTF-8" ) );
    zip.closeEntry();
  }

  private boolean findAmdDefine( InputStream is, ArrayList<String> exports ) {
    final Pattern definePattern =
        Pattern.compile( "\bdefine\b(\\s*)\\(((\\s*)\"[^\"]+\"(\\s*),)?((\\s*)\\[((\\s*)\"[^\"]+\""
            + "(\\s*),?)+(\\s*)\\](\\s*),)?((\\s*)function)" );

    final Pattern globalPattern =
        Pattern.compile(
            "(\\bwindow\\b|\\bexports\\b)\\.(([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*)"
                + "\\s*=\\s*[\\w${][^,;]+" );

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
          final String varSegment = var.split( "\\.", 2 )[ 0 ];
          if ( !varSegment.startsWith( "on" ) && !JS_KNOWN_GLOBALS.contains( varSegment ) && !exports
              .contains( var ) ) {
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
}
