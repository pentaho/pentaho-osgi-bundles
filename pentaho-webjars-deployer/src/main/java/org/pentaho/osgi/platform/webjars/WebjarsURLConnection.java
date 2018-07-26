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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.osgi.framework.Constants;
import org.pentaho.osgi.platform.webjars.utils.RequireJsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Created by nbaker on 9/6/14.
 */
public class WebjarsURLConnection extends URLConnection {

  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool( 5, r -> {
    Thread thread = Executors.defaultThreadFactory().newThread( r );
    thread.setDaemon( true );
    thread.setName( "WebjarsURLConnection pool" );
    return thread;
  } );

  Future<Void> transform_thread;

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  private final boolean minificationEnabled;

  public WebjarsURLConnection( URL url ) {
    this( url, true );
  }

  public WebjarsURLConnection( URL url, boolean minificationEnabled ) {
    super( url );

    this.minificationEnabled = minificationEnabled;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public InputStream getInputStream() throws IOException {
    try {
      final PipedOutputStream pipedOutputStream = new PipedOutputStream();
      PipedInputStream pipedInputStream = new PipedInputStream( pipedOutputStream );

      // making this here allows to fail with invalid URLs
      final URLConnection urlConnection = url.openConnection();
      urlConnection.connect();
      final InputStream originalInputStream = urlConnection.getInputStream();

      transform_thread = EXECUTOR.submit( new WebjarsTransformer( url, originalInputStream, pipedOutputStream, this.minificationEnabled ) );

      return pipedInputStream;
    } catch ( Exception e ) {
      logger.error( getURL().toString() + ": Error opening url" );

      throw new IOException( "Error opening url", e );
    }
  }

  private static class WebjarsTransformer implements Callable<Void> {
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

    private static final int BYTES_BUFFER_SIZE = 4096;
    private static final String WEBJAR_SRC_ALIAS_PREFIX = "webjar-src";
    private static final String MINIFIED_RESOURCES_OUTPUT_PATH = "META-INF/resources/dist-gen";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    /* constructor information */
    private final URL url;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final boolean minificationEnabled;

    //region transformation state

    /* artifact information */
    private RequireJsGenerator.ArtifactInfo artifactInfo;
    private JarOutputStream jarOutputStream;
    private boolean isClassicWebjar;
    private boolean isNpmWebjar;
    private boolean isBowerWebjar;
    // version read from pom file, used as fallback in bower webjars without version information
    private String pomProjectVersion;

    /* require config information */
    private boolean wasReadFromPom;
    private RequireJsGenerator requireConfig;

    /* AMD module information */
    private ArrayList<String> exportedGlobals;
    private boolean isAmdPackage;

    /* source map location cache */
    private String lastPrefix;
    private SourceMap.LocationMapping lastLocationMapping;

    /* other */
    private String webjarUrl;

    /* resource paths */
    private String packageNameFromResourcesPath;
    private String packageVersionFromResourcesPath;

    private String relativeResourcesPath;
    private Path absoluteResourcesPath;

    private Path absoluteTempPath;

    //endregion

    WebjarsTransformer( URL url, InputStream inputStream, PipedOutputStream outputStream, boolean minificationEnabled ) {
      this.url = url;

      this.inputStream = inputStream;
      this.outputStream = outputStream;

      this.minificationEnabled = minificationEnabled;
    }

    @Override
    public Void call() throws Exception {
      try {
        this.transform();
      } catch ( Exception e ) {
        logger.error( this.url.toString() + ": Error Transforming zip", e );

        this.outputStream.close();

        throw e;
      }

      return null;
    }

    private void transform() throws IOException {
      JarInputStream jarInputStream = new JarInputStream( inputStream );

      try {
        init();

        extractArtifactInfo( this.url );

        this.jarOutputStream = new JarOutputStream( outputStream, getManifest( artifactInfo, jarInputStream ) );

        boolean minificationFailed = false;

        Map<String, Object> overrides = RequireJsGenerator.getPackageOverrides( artifactInfo.getGroup(), artifactInfo.getArtifactId(), artifactInfo.getVersion() );

        Map<String, Map<String, String>> wrap;
        if ( overrides != null ) {
          // if there is an explicit override file, don't try to be smart and just assume it's AMD ready
          isAmdPackage = true;

          wrap = (Map<String, Map<String, String>>) overrides.getOrDefault( "wrap", Collections.<String, Map<String, String>>emptyMap() );
        } else {
          // empty map, for simplicity
          wrap = Collections.emptyMap();
        }

        ZipEntry entry;
        while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
          String name = entry.getName();

          if ( name.endsWith( MANIFEST_MF ) ) {
            // ignore existing manifest, we already created our own
            jarInputStream.closeEntry();
            continue;
          }

          if ( name.endsWith( POM_NAME ) && POM_PATTERN.matcher( name ).matches() ) {
            handlePomFile( jarInputStream );

            // the pom file will not be copied to the generated bundle,
            // because it would be troublesome as the entry stream is already consumed
            // and it isn't really needed anyway
            continue;
          }

          boolean isPackageFile = isPackageFile( name );

          if ( !entry.isDirectory() ) {
            File temporarySourceFile = null;
            BufferedOutputStream temporarySourceFileOutputStream = null;

            String pre = "";
            String pos = "";

            if ( isPackageFile ) {
              // only save to the temp folder resources from the package's source folder
              temporarySourceFile = new File( absoluteTempPath.toAbsolutePath() + File.separator + FilenameUtils.separatorsToSystem( name ) );

              //noinspection ResultOfMethodCallIgnored
              temporarySourceFile.getParentFile().mkdirs();

              temporarySourceFileOutputStream = new BufferedOutputStream( new FileOutputStream( temporarySourceFile ) );

              String fileRelativePath = "/" + absoluteResourcesPath.relativize( temporarySourceFile.toPath() );
              fileRelativePath = fileRelativePath.replaceAll( "\\\\", "/" );
              if ( wrap.containsKey( fileRelativePath ) ) {
                Map<String, String> wrapCode = wrap.get( fileRelativePath );
                pre = wrapCode.getOrDefault( "pre", "" );
                pos = wrapCode.getOrDefault( "pos", "" );
              }
            }

            //region Copy the file from the source jar to the generated jar
            ZipEntry zipEntry = new ZipEntry( name );
            jarOutputStream.putNextEntry( zipEntry );

            if ( isPackageFile && pre.length() > 0 ) {
              temporarySourceFileOutputStream.write( pre.getBytes() );
              temporarySourceFileOutputStream.write( "\n".getBytes() );

              jarOutputStream.write( pre.getBytes() );
              jarOutputStream.write( "\n".getBytes() );
            }

            byte[] bytes = new byte[ BYTES_BUFFER_SIZE ];
            int read;
            while ( ( read = jarInputStream.read( bytes ) ) != -1 ) {
              if ( isPackageFile ) {
                // also output to the temp source file
                temporarySourceFileOutputStream.write( bytes, 0, read );
              }

              jarOutputStream.write( bytes, 0, read );
            }

            if ( isPackageFile && pos.length() > 0 ) {
              temporarySourceFileOutputStream.write( "\n".getBytes() );
              temporarySourceFileOutputStream.write( pos.getBytes() );
              temporarySourceFileOutputStream.write( "\n".getBytes() );

              jarOutputStream.write( "\n".getBytes() );
              jarOutputStream.write( pos.getBytes() );
              jarOutputStream.write( "\n".getBytes() );
            }

            jarOutputStream.closeEntry();
            // endregion

            if ( isPackageFile ) {
              temporarySourceFileOutputStream.close();

              if ( !isAmdPackage && isJsFile( name ) && RequireJsGenerator.findAmdDefine( new FileInputStream( temporarySourceFile ), exportedGlobals ) ) {
                isAmdPackage = true;
              }
            }

            if ( requireConfig == null || ( isClassicWebjar && !wasReadFromPom ) ) {
              if ( isClassicWebjar && name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
                // next comes the module author's webjars-requirejs.js
                handleWebjarsRequireJS( name, temporarySourceFile );
              } else if ( isNpmWebjar && name.endsWith( NPM_NAME ) ) {
                // try to generate requirejs.json from package.json (Npm WebJars) or bower.json (Bower WebJars)
                handlePackageJson( name, temporarySourceFile );
              } else if ( isBowerWebjar && name.endsWith( BOWER_NAME ) ) {
                // try to generate requirejs.json from package.json (Npm WebJars) or bower.json (Bower WebJars)
                handleBowerJson( name, temporarySourceFile );
              }
            }
          }

          jarInputStream.closeEntry();
        }

        // nothing more to do if there aren't any source files
        if ( absoluteResourcesPath != null ) {
          Collection<File> scrFiles = FileUtils.listFiles(
              absoluteResourcesPath.toFile(),
              TrueFileFilter.INSTANCE,
              TrueFileFilter.INSTANCE
          );

          if ( this.minificationEnabled ) {
            try {
              CompilerOptions options = initCompilationResources();

              for ( File srcFile : scrFiles ) {
                final String name = srcFile.getName();

                if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
                  continue;
                }

                final String relSrcFilePath = FilenameUtils.separatorsToUnix( absoluteResourcesPath.relativize( srcFile.toPath() ).toString() );
                final String relOutFilePath = MINIFIED_RESOURCES_OUTPUT_PATH + "/" + relSrcFilePath;

                if ( isJsFile( name ) ) {
                  // Check if there is a .map with the same name
                  // if so, assume it is already minified and just copy both files
                  File mapFile = new File( name + ".map", srcFile.getParent() );
                  if ( mapFile.exists() ) {
                    copyFileToZip( jarOutputStream, relOutFilePath, srcFile );
                    copyFileToZip( jarOutputStream, relOutFilePath + ".map", mapFile );
                    continue;
                  }

                  options.setSourceMapLocationMappings( getLocationMappings( srcFile.toPath().getParent(), absoluteResourcesPath, packageNameFromResourcesPath, packageVersionFromResourcesPath ) );

                  try {
                    CompiledScript compiledScript = new CompiledScript( srcFile, relSrcFilePath, options ).invoke();

                    addContentToZip( jarOutputStream, relOutFilePath, compiledScript.getCode() );
                    addContentToZip( jarOutputStream, relOutFilePath + ".map", compiledScript.getSourcemap() );
                  } catch ( Exception failedCompilationException ) {
                    logger.warn( webjarUrl + ": error minifing " + relSrcFilePath + ", copied original version" );

                    copyFileToZip( jarOutputStream, relOutFilePath, srcFile );
                  }
                } else if ( !isMapFile( name ) ) {
                  // just copy all resources (except .map files)
                  copyFileToZip( jarOutputStream, relOutFilePath, srcFile );
                }
              }
            } catch ( Exception e ) {
              minificationFailed = true;

              logger.warn( webjarUrl + ": exception minifing, serving original files", e );
            }
          }

          if ( requireConfig == null ) {
            // in last resort generate requirejs config by mapping the root path
            requireConfig = RequireJsGenerator.emptyGenerator( packageNameFromResourcesPath, packageVersionFromResourcesPath );

            logger.warn( webjarUrl + ": malformed webjar deployed using root path mapping" );
          }

          if ( requireConfig != null ) {
            try {
              final String exports = !isAmdPackage && !exportedGlobals.isEmpty() ? exportedGlobals.get( 0 ) : null;

              final RequireJsGenerator.ModuleInfo moduleInfo =
                  requireConfig.getConvertedConfig( artifactInfo, isAmdPackage, exports, overrides );

              addContentToZip( jarOutputStream, PENTAHO_RJS_LOCATION, moduleInfo.exportRequireJs() );

              try {
                String blueprintTemplate;
                if ( !this.minificationEnabled || minificationFailed ) {
                  blueprintTemplate = generateBlueprintWithoutMinifiedResources( relativeResourcesPath, moduleInfo );
                } else {
                  blueprintTemplate = generateBlueprint( relativeResourcesPath, moduleInfo );
                }

                addContentToZip( jarOutputStream, "OSGI-INF/blueprint/blueprint.xml", blueprintTemplate );
              } catch ( Exception e ) {
                logger.error( webjarUrl + ": error saving OSGI-INF/blueprint/blueprint.xml - " + e.getMessage() );
              }
            } catch ( Exception e ) {
              logger.error( webjarUrl + ": error saving " + PENTAHO_RJS_LOCATION + " - " + e.getMessage() );
            }
          }
        }

        try {
          jarOutputStream.closeEntry();

          outputStream.flush();

          jarOutputStream.close();
        } catch ( IOException ioexception ) {
          logger.debug( webjarUrl + ": " + DEBUG_MESSAGE_FAILED_WRITING, ioexception );
        }
      } catch ( IOException e ) {
        logger.debug( webjarUrl + ": Pipe is closed, no need to continue." );
      } finally {
        try {
          FileUtils.deleteDirectory( absoluteTempPath.toFile() );
        } catch ( IOException ignored ) {
          // ignored
        }

        try {
          jarInputStream.close();
        } catch ( IOException ioexception ) {
          logger.debug( webjarUrl + ": Tried to close JarInputStream, but it was already closed.", ioexception );
        }
      }
    }

    private void init() throws IOException {
      this.wasReadFromPom = false;
      this.requireConfig = null;

      this.exportedGlobals = new ArrayList<>();
      this.isAmdPackage = false;

      this.pomProjectVersion = null;

      this.packageNameFromResourcesPath = null;
      this.packageVersionFromResourcesPath = null;
      this.relativeResourcesPath = null;
      this.absoluteResourcesPath = null;

      this.absoluteTempPath = Files.createTempDirectory( "WebjarsURLConnection" );
    }

    private void extractArtifactInfo( URL url ) {
      this.artifactInfo = new RequireJsGenerator.ArtifactInfo( url );

      this.isClassicWebjar = artifactInfo.getGroup().equals( "org.webjars" );

      this.isNpmWebjar = artifactInfo.getGroup().equals( "org.webjars.npm" );
      this.isBowerWebjar = artifactInfo.getGroup().equals( "org.webjars.bower" );

      this.webjarUrl = url.toString();
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

    private void handlePomFile( JarInputStream jarInputStream ) {
      if ( isClassicWebjar ) {
        // handcrafted requirejs configuration on pom.xml has top prioriy in Classic WebJars
        try {
          requireConfig = RequireJsGenerator.parsePom( jarInputStream );
          wasReadFromPom = true;

          logger.debug( webjarUrl + ": Classic WebJar -> requirejs configuration from pom.xml" );
        } catch ( Exception ignored ) {
          // ignored
        }
      } else if ( isBowerWebjar ) {
        // collect from the pom file just the version information, for fallback in bower webjars
        try {
          pomProjectVersion = RequireJsGenerator.getWebjarVersionFromPom( jarInputStream );

          // fill in the version if bower.json was already processed
          if ( requireConfig != null
              && requireConfig.getModuleInfo() != null
              && requireConfig.getModuleInfo().getVersion() == null ) {
            requireConfig.getModuleInfo().setVersion( pomProjectVersion );
          }
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private void handleWebjarsRequireJS( String name, File temporarySourceFile ) {
      Matcher matcher = MODULE_PATTERN.matcher( name );
      if ( matcher.matches() ) {
        try {
          requireConfig = RequireJsGenerator.processJsScript(
              matcher.group( 1 ), matcher.group( 2 ), new FileInputStream( temporarySourceFile ) );

          logger.debug( webjarUrl + ": Classic WebJar -> read requirejs configuration from webjars-requirejs.js" );
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private void handlePackageJson( String name, File temporarySourceFile ) {
      if ( NPM_PATTERN.matcher( name ).matches() ) {
        try {
          requireConfig = RequireJsGenerator.parseJsonPackage( new FileInputStream( temporarySourceFile ) );

          logger.debug( webjarUrl + ": NPM WebJar -> read requirejs configuration from package.json" );
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private void handleBowerJson( String name, File temporarySourceFile ) {
      if ( BOWER_PATTERN.matcher( name ).matches() ) {
        try {
          requireConfig = RequireJsGenerator.parseJsonPackage( new FileInputStream( temporarySourceFile ) );

          // on bower webjars, check if the version information is present
          // if not fill it with the version extracted from pom, if already gathered
          if ( pomProjectVersion != null && requireConfig != null
              && requireConfig.getModuleInfo() != null && requireConfig.getModuleInfo().getVersion() == null ) {
            requireConfig.getModuleInfo().setVersion( pomProjectVersion );
          }

          logger.debug( webjarUrl + ": Bower WebJar -> read requirejs configuration from bower.json" );
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private boolean isPackageFile( String name ) {
      boolean isPackageFile = false;

      // store the path of the first file that fits de expected folder structure,
      // for physical path mapping and also fallback on malformed webjars
      if ( packageNameFromResourcesPath == null ) {
        Matcher matcher = PACKAGE_FILES_PATTERN.matcher( name );

        if ( matcher.matches() ) {
          packageNameFromResourcesPath = matcher.group( 1 );
          packageVersionFromResourcesPath = matcher.group( 2 );

          relativeResourcesPath = "META-INF/resources/webjars/" + packageNameFromResourcesPath + "/" + packageVersionFromResourcesPath;
          absoluteResourcesPath = absoluteTempPath.resolve( FilenameUtils.separatorsToSystem( relativeResourcesPath ) );

          isPackageFile = true;
        }
      } else {
        isPackageFile = name.contains( packageNameFromResourcesPath + "/" + packageVersionFromResourcesPath );
      }

      return isPackageFile;
    }

    private String generateBlueprint( String relSrcPath, RequireJsGenerator.ModuleInfo moduleInfo ) throws IOException {
      String blueprintTemplate;
      blueprintTemplate =
          IOUtils.toString( getClass().getResourceAsStream(
              "/org/pentaho/osgi/platform/webjars/blueprint-minified-template.xml" ) );

      blueprintTemplate = blueprintTemplate.replace( "{src_path}", relSrcPath );
      blueprintTemplate = blueprintTemplate
          .replace( "{src_alias}", WEBJAR_SRC_ALIAS_PREFIX + "/" + moduleInfo.getVersionedPath() );

      blueprintTemplate = blueprintTemplate.replace( "{dist_path}", MINIFIED_RESOURCES_OUTPUT_PATH );
      blueprintTemplate = blueprintTemplate.replace( "{dist_alias}", moduleInfo.getVersionedPath() );
      return blueprintTemplate;
    }

    private String generateBlueprintWithoutMinifiedResources( String relSrcPath, RequireJsGenerator.ModuleInfo moduleInfo ) throws IOException {
      String blueprintTemplate;
      blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream(
          "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );

      blueprintTemplate = blueprintTemplate.replace( "{dist_path}", relSrcPath );
      blueprintTemplate = blueprintTemplate.replace( "{dist_alias}", moduleInfo.getVersionedPath() );
      return blueprintTemplate;
    }

    private boolean isJsFile( String name ) {
      return name.endsWith( ".js" );
    }

    private boolean isMapFile( String name ) {
      return name.endsWith( ".map" );
    }

    private void copyFileToZip( JarOutputStream zip, String entry, File file ) throws IOException {
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

    private void addContentToZip( JarOutputStream zip, String entry, String content ) throws IOException {
      ZipEntry zipEntry = new ZipEntry( entry );
      zip.putNextEntry( zipEntry );
      zip.write( content.getBytes( "UTF-8" ) );
      zip.closeEntry();
    }

    private CompilerOptions initCompilationResources() {
      CompilerOptions options = new CompilerOptions();
      CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel( options );

      options.setSourceMapOutputPath( "." );

      WarningLevel.QUIET.setOptionsForWarningLevel( options );
      options.setWarningLevel( DiagnosticGroups.LINT_CHECKS, CheckLevel.OFF );

      options.setLanguageIn( CompilerOptions.LanguageMode.ECMASCRIPT5 );

      // make sure these are clear
      this.lastPrefix = null;
      this.lastLocationMapping = null;

      return options;
    }

    private List<SourceMap.LocationMapping> getLocationMappings( Path srcFileFolder, Path absSrcPath, String packageName, String packageVersion ) {
      // reuses the lastLocationMapping if the script's folder is the same than the previous processed script

      final String prefix = FilenameUtils.separatorsToUnix( srcFileFolder.toString() );
      if ( lastPrefix == null || !lastPrefix.equals( prefix ) ) {
        String relPath = FilenameUtils.separatorsToUnix( absSrcPath.relativize( srcFileFolder ).toString() );
        if ( !relPath.isEmpty() ) {
          relPath = "/" + relPath;
        }

        String reverseRelPath = FilenameUtils.separatorsToUnix( srcFileFolder.relativize( absSrcPath ).toString() );
        if ( !reverseRelPath.isEmpty() ) {
          reverseRelPath += "/";
        }

        final String replacement =
            "../../" + reverseRelPath + WEBJAR_SRC_ALIAS_PREFIX + "/"
                + packageName + "/"
                + packageVersion + relPath;

        lastLocationMapping = new SourceMap.LocationMapping( prefix, replacement );

        lastPrefix = prefix;
      }

      List<SourceMap.LocationMapping> lms = new ArrayList<>();
      lms.add( lastLocationMapping );
      return lms;
    }

    private static class CompiledScript {
      private static final SourceFile EMPTY_EXTERNS_SOURCE_FILE = SourceFile.fromCode( "externs.js", "" );

      private final File srcFile;
      private final String relSrcFilePath;
      private final CompilerOptions options;

      private StringBuilder code;
      private StringBuilder sourcemap;

      CompiledScript( File srcFile, String relSrcFilePath, CompilerOptions options ) {
        this.srcFile = srcFile;
        this.relSrcFilePath = relSrcFilePath;
        this.options = options;
      }

      String getCode() {
        return code.toString();
      }

      String getSourcemap() {
        return sourcemap.toString();
      }

      CompiledScript invoke() throws Exception {
        Compiler compiler = new Compiler();

        Compiler.setLoggingLevel( Level.OFF );

        SourceFile input = SourceFile.fromFile( srcFile );
        input.setOriginalPath( relSrcFilePath );

        Result res = compiler.compile( EMPTY_EXTERNS_SOURCE_FILE, input, options );
        if ( res.success ) {
          String name = srcFile.getName();

          code = new StringBuilder( compiler.toSource() );
          code.append( "\n//# sourceMappingURL=" ).append( name ).append( ".map" );

          sourcemap = new StringBuilder();
          SourceMap sm = compiler.getSourceMap();
          sm.appendTo( sourcemap, name );

          return this;
        }

        throw new Exception( "Failed script compilation" );
      }
    }
  }
}
