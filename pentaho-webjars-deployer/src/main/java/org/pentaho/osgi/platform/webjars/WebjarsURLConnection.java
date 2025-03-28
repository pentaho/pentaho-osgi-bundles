/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.platform.webjars;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool( 5, r -> {
    Thread thread = Executors.defaultThreadFactory().newThread( r );
    thread.setDaemon( true );
    thread.setName( "WebjarsURLConnection pool" );
    return thread;
  } );

  Future<Void> transform_thread;

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  private final boolean automaticNonAmdShimConfigEnabled;

  public WebjarsURLConnection( URL url ) {
    this( url, false );
  }

  public WebjarsURLConnection( URL url, boolean automaticNonAmdShimConfigEnabled ) {
    super( url );

    this.automaticNonAmdShimConfigEnabled = automaticNonAmdShimConfigEnabled;
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

      transform_thread = EXECUTOR.submit( new WebjarsTransformer( url, originalInputStream, pipedOutputStream, this.automaticNonAmdShimConfigEnabled ) );

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

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    /* constructor information */
    private final URL url;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final boolean automaticNonAmdShimConfigEnabled;

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

    /* other */
    private String webjarUrl;

    /* resource paths */
    private String packageNameFromResourcesPath;
    private String packageVersionFromResourcesPath;

    private String relativeResourcesPath;

    private Path absoluteTempPath;

    private boolean temporarySourceFileIsNeeded;

    //endregion

    WebjarsTransformer( URL url, InputStream inputStream, PipedOutputStream outputStream, boolean automaticNonAmdShimConfigEnabled ) {
      this.url = url;

      this.inputStream = inputStream;
      this.outputStream = outputStream;

      this.automaticNonAmdShimConfigEnabled = automaticNonAmdShimConfigEnabled;
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

      init();

      try {
        extractArtifactInfo( this.url );

        this.jarOutputStream = new JarOutputStream( outputStream, getManifest( artifactInfo, jarInputStream ) );

        Map<String, Object> overrides = RequireJsGenerator.getPackageOverrides( artifactInfo.getGroup(), artifactInfo.getArtifactId(), artifactInfo.getVersion() );

        Map<String, Map<String, String>> wrap = getWrap( overrides );

        boolean packageHasContent = false;

        ZipEntry entry;
        while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
          String name = entry.getName();

          if ( name.endsWith( MANIFEST_MF ) ) {
            // ignore existing manifest, we already created our own
            jarInputStream.closeEntry();
            continue;
          }

          if ( handlePackageDescriptor( name, jarInputStream ) ) {
            // the descriptor file will not be copied to the generated bundle,
            // because it would be troublesome as the entry stream is already consumed
            // and it isn't really needed anyway
            jarInputStream.closeEntry();
            continue;
          }

          if ( !isPackageFile( name ) ) {
            // resources outside the META-INF/resources/webjars/** won't be available so no need to copy them
            jarInputStream.closeEntry();
            continue;
          }

          if ( !entry.isDirectory() ) {
            File temporarySourceFile = null;
            BufferedOutputStream temporarySourceFileOutputStream = null;

            String pre = "";
            String pos = "";

            if( this.temporarySourceFileIsNeeded ) {
              // only save to the temp folder resources from the package's source folder
              temporarySourceFile = new File( absoluteTempPath.toAbsolutePath() + File.separator + FilenameUtils.separatorsToSystem( name ) );

              //noinspection ResultOfMethodCallIgnored
              temporarySourceFile.getParentFile().mkdirs();

              temporarySourceFileOutputStream = new BufferedOutputStream( new FileOutputStream( temporarySourceFile ) );
            }

            String fileRelativePath = name.substring( relativeResourcesPath.length() );
            if ( wrap.containsKey( fileRelativePath ) ) {
              Map<String, String> wrapCode = wrap.get( fileRelativePath );
              pre = wrapCode.getOrDefault( "pre", "" );
              pos = wrapCode.getOrDefault( "pos", "" );
            }

            //region Copy the file from the source jar to the generated jar
            ZipEntry zipEntry = new ZipEntry( name );
            jarOutputStream.putNextEntry( zipEntry );

            if ( pre.length() > 0 ) {
              this.writeToOutput( temporarySourceFileOutputStream, (pre + "\n").getBytes() );
            }

            byte[] bytes = new byte[ BYTES_BUFFER_SIZE ];
            int read;
            while ( ( read = jarInputStream.read( bytes ) ) != -1 ) {
              this.writeToOutput( temporarySourceFileOutputStream, bytes, read );
            }

            if ( pos.length() > 0 ) {
              this.writeToOutput( temporarySourceFileOutputStream, ("\n" + pos + "\n").getBytes() );
            }

            jarOutputStream.closeEntry();
            // endregion

            if( this.temporarySourceFileIsNeeded ) {
              temporarySourceFileOutputStream.close();
            }

            if ( !this.isAmdPackage && isJsFile( name ) && RequireJsGenerator.findAmdDefine( new FileInputStream( temporarySourceFile ), exportedGlobals ) ) {
              this.isAmdPackage = true;
            }

            packageHasContent = true;
          }

          jarInputStream.closeEntry();
        }

        // nothing more to do if there aren't any source files
        if ( packageHasContent ) {
          if ( requireConfig == null ) {
            // in last resort generate requirejs config by mapping the root path
            requireConfig = RequireJsGenerator.emptyGenerator( packageNameFromResourcesPath, packageVersionFromResourcesPath );

            logger.warn( webjarUrl + ": malformed webjar deployed using root path mapping" );
          }

          if ( requireConfig != null ) {

            try {
              final String exports = !this.isAmdPackage && !exportedGlobals.isEmpty() ? exportedGlobals.get( 0 ) : null;

              final RequireJsGenerator.ModuleInfo moduleInfo =
                  requireConfig.getConvertedConfig( artifactInfo, this.isAmdPackage, exports, overrides );

              addContentToZip( jarOutputStream, PENTAHO_RJS_LOCATION, moduleInfo.exportRequireJs() );

              try {
                String blueprintTemplate;
                blueprintTemplate = generateBlueprint( relativeResourcesPath, moduleInfo );

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
        if ( this.absoluteTempPath != null ) {
          try {
            FileUtils.deleteDirectory( absoluteTempPath.toFile() );
          } catch ( IOException ignored ) {
            // ignored
          } finally {
            this.absoluteTempPath = null;
          }
        }

        try {
          jarInputStream.close();
        } catch ( IOException ioexception ) {
          logger.debug( webjarUrl + ": Tried to close JarInputStream, but it was already closed.", ioexception );
        }
      }
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, Map<String, String>> getWrap( Map<String, Object> overrides ) {
      Map<String, Map<String, String>> wrap = null;
      if ( overrides != null ) {
        this.isAmdPackage = true;
        wrap = (Map<String, Map<String, String>>) overrides.get( "wrap" );
      }
      return wrap == null ? Collections.emptyMap() : wrap;
    }

    private void writeToOutput( BufferedOutputStream temporarySourceFileOutputStream, byte[] bytes, int read ) throws IOException {
      if ( this.temporarySourceFileIsNeeded ) {
        // also output to the temp source file
        temporarySourceFileOutputStream.write( bytes, 0, read );
      }

      jarOutputStream.write( bytes, 0, read );
    }

    private void writeToOutput( BufferedOutputStream temporarySourceFileOutputStream, byte[] bytes ) throws IOException {
      this.writeToOutput( temporarySourceFileOutputStream, bytes, bytes.length );
    }


    /**
     * @param name the potential package descriptor file name
     * @param inputStream the stream from where to read the package descriptor file contents
     *
     * @return true if a package descriptor was successfully read and processed, false otherwise
     */
    private boolean handlePackageDescriptor( String name, InputStream inputStream ) {
      if ( name.endsWith( POM_NAME ) && POM_PATTERN.matcher( name ).matches() ) {
        // try to generate requirejs.json from the pom.xml file (Classic WebJars)
        // handcrafted requirejs configuration on pom.xml has priority over the author's webjars-requirejs.js
        // (also collect from the POM file the version, for fallback in Bower WebJars without version information)
        handlePomFile( inputStream );
        return true;
      }

      if ( requireConfig == null || ( isClassicWebjar && !wasReadFromPom ) ) {
        if ( isClassicWebjar && name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
          // try to generate requirejs.json from the module author's webjars-requirejs.js (Classic WebJars)
          handleWebjarsRequireJS( name, inputStream );
          return true;
        } else if ( isNpmWebjar && name.endsWith( NPM_NAME ) ) {
          // try to generate requirejs.json from the package.json file (Npm WebJars)
          handlePackageJson( name, inputStream );
          return true;
        } else if ( isBowerWebjar && name.endsWith( BOWER_NAME ) ) {
          // try to generate requirejs.json from the bower.json file (Bower WebJars)
          handleBowerJson( name, inputStream );
          return true;
        }
      }

      return false;
    }

    private void init() throws IOException {
      this.wasReadFromPom = false;
      this.requireConfig = null;

      this.exportedGlobals = new ArrayList<>();

      // if the automatic non-AMD shim config is enabled then the package is not AMD until proven otherwise;
      // if disabled  just assume the code is AMD ready
      this.isAmdPackage = !this.automaticNonAmdShimConfigEnabled;

      this.pomProjectVersion = null;

      this.packageNameFromResourcesPath = null;
      this.packageVersionFromResourcesPath = null;
      this.relativeResourcesPath = null;

      this.temporarySourceFileIsNeeded = this.automaticNonAmdShimConfigEnabled;

      if ( this.temporarySourceFileIsNeeded ) {
        this.absoluteTempPath = Files.createTempDirectory( "WebjarsURLConnection" );
      }
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
      manifest.getMainAttributes().putValue( Constants.BUNDLE_MANIFESTVERSION, "2" );
      manifest.getMainAttributes()
          .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webjars-" + artifactInfo.getArtifactId() );

      manifest.getMainAttributes()
          .put( new Attributes.Name( Constants.BUNDLE_VERSION ), artifactInfo.getOsgiCompatibleVersion() );
      return manifest;
    }

    private void handlePomFile( InputStream jarInputStream ) {
      if ( isClassicWebjar ) {
        // handcrafted requirejs configuration on pom.xml has top priority in Classic WebJars
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

    private void handleWebjarsRequireJS( String name, InputStream inputStream ) {
      Matcher matcher = MODULE_PATTERN.matcher( name );
      if ( matcher.matches() ) {
        try {
          requireConfig = RequireJsGenerator.processJsScript(
              matcher.group( 1 ), matcher.group( 2 ), inputStream );

          logger.debug( webjarUrl + ": Classic WebJar -> read requirejs configuration from webjars-requirejs.js" );
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private void handlePackageJson( String name, InputStream inputStream ) {
      if ( NPM_PATTERN.matcher( name ).matches() ) {
        try {
          requireConfig = RequireJsGenerator.parseJsonPackage( inputStream );

          logger.debug( webjarUrl + ": NPM WebJar -> read requirejs configuration from package.json" );
        } catch ( Exception ignored ) {
          // ignored
        }
      }
    }

    private void handleBowerJson( String name, InputStream inputStream ) {
      if ( BOWER_PATTERN.matcher( name ).matches() ) {
        try {
          requireConfig = RequireJsGenerator.parseJsonPackage( inputStream );

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

          isPackageFile = true;
        }
      } else {
        isPackageFile = name.contains( packageNameFromResourcesPath + "/" + packageVersionFromResourcesPath );
      }

      return isPackageFile;
    }

    private String generateBlueprint( String relSrcPath, RequireJsGenerator.ModuleInfo moduleInfo ) throws IOException {
      String blueprintTemplate;
      blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream(
          "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ), StandardCharsets.UTF_8 );

      blueprintTemplate = blueprintTemplate.replace( "{dist_path}", relSrcPath );
      blueprintTemplate = blueprintTemplate.replace( "{dist_alias}", moduleInfo.getVersionedPath() );
      return blueprintTemplate;
    }

    private boolean isJsFile( String name ) {
      return name.endsWith( ".js" );
    }


    private void addContentToZip( JarOutputStream zip, String entry, String content ) throws IOException {
      ZipEntry zipEntry = new ZipEntry( entry );
      zip.putNextEntry( zipEntry );
      zip.write( content.getBytes( StandardCharsets.UTF_8 ) );
      zip.closeEntry();
    }

  }
}
