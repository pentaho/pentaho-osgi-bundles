/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.deployer.archive.impl;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.parser.JSONParser;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.pentaho.webpackage.core.PentahoWebPackageConstants.CAPABILITY_NAMESPACE;

public class WebPackageURLConnection extends java.net.URLConnection {
  public static final String URL_PROTOCOL = "pentaho-webpackage";
  public static final String PACKAGE_JSON = "package.json";

  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool( 5, r -> {
    Thread thread = Executors.defaultThreadFactory().newThread( r );
    thread.setDaemon( true );
    thread.setName( "WebjarsURLConnection pool" );
    return thread;
  } );

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  Future<Void> transform_thread;

  public WebPackageURLConnection( URL url ) {
    super( url );
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
      final java.net.URLConnection urlConnection = this.url.openConnection();
      urlConnection.connect();
      final InputStream originalInputStream = urlConnection.getInputStream();

      this.transform_thread = EXECUTOR.submit( new WebPackageTransformer( this.url, originalInputStream, pipedOutputStream ) );

      return pipedInputStream;
    } catch ( Exception e ) {
      this.logger.error( getURL().toString() + ": Error opening url" );

      throw new IOException( "Error opening url", e );
    }
  }

  private static class WebPackageTransformer implements Callable<Void> {
    private static final String DEBUG_MESSAGE_FAILED_WRITING =
        "Problem transferring Jar content, probably JarOutputStream was already closed.";

    private static final int BYTES_BUFFER_SIZE = 4096;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    /* constructor information */
    private final URL url;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    //region transformation state

    /* artifact information */
    private JarOutputStream jarOutputStream;

    /* resource paths */
    private Path absoluteTempPath;

    // those are needed until PAXWEB-1099 is in platform, to avoid the "same servlet name" issue
    private String resourcesFolderName;
    private Path absoluteTempResourcesPath;

    //endregion

    WebPackageTransformer( URL url, InputStream inputStream, PipedOutputStream outputStream ) {
      this.url = url;

      this.inputStream = inputStream;
      this.outputStream = outputStream;
    }

    @Override
    public Void call() throws Exception {
      try {
        this.transform();
      } catch ( Exception e ) {
        this.logger.error( this.url.toString() + ": Error Transforming package", e );

        this.outputStream.close();

        throw e;
      }

      return null;
    }

    private void transform() throws IOException {
      init();

      try {
        List<String> capabilities = new ArrayList<>();
        List<String> requirements = null; // new ArrayList<>();

        if ( this.url.getProtocol().equals( "jardir" ) || this.url.getProtocol().equals( "file" ) && this.url.getPath().endsWith( ".zip" ) ) {
          processZipArchive( capabilities, requirements );
        } else {
          processTgzArchive( capabilities, requirements );
        }

        Manifest manifest = createManifest();

        if ( !capabilities.isEmpty() ) {
          manifest.getMainAttributes()
              .put( new Attributes.Name( Constants.PROVIDE_CAPABILITY ), String.join( ", ", capabilities ) );
        }

//        if ( !requirements.isEmpty() ) {
//          manifest.getMainAttributes()
//              .put( new Attributes.Name( Constants.REQUIRE_CAPABILITY ), String.join( ", ", requirements ) );
//        }

        this.jarOutputStream = new JarOutputStream( this.outputStream, manifest );

        Collection<File> scrFiles = FileUtils.listFiles(
            this.absoluteTempPath.toFile(),
            TrueFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE
        );

        for ( File srcFile : scrFiles ) {
          final String relSrcFilePath = FilenameUtils.separatorsToUnix( this.absoluteTempPath.relativize( srcFile.toPath() ).toString() );

          copyFileToZip( this.jarOutputStream, relSrcFilePath, srcFile );
        }

        try {
          this.jarOutputStream.closeEntry();

          this.outputStream.flush();

          this.jarOutputStream.close();
        } catch ( IOException ioexception ) {
          this.logger.debug( DEBUG_MESSAGE_FAILED_WRITING, ioexception );
        }
      } catch ( IOException e ) {
        this.logger.debug( ": Pipe is closed, no need to continue." );
      } finally {
        try {
          FileUtils.deleteDirectory( this.absoluteTempPath.toFile() );
        } catch ( IOException ignored ) {
          // ignored
        }
      }
    }

    private void processZipArchive( List<String> capabilities, List<String> requirements ) throws IOException {
      ZipInputStream zipInputStream = null;

      try {
        zipInputStream = new ZipInputStream( this.inputStream );

        ZipEntry entry;
        while ( ( entry = zipInputStream.getNextEntry() ) != null ) {
          String name = entry.getName();

          // filter out macOS zip metadata files
          if ( !entry.isDirectory() && !name.startsWith( "__MACOSX/" ) ) {
            processArchiveEntry( zipInputStream, name, capabilities, requirements );
          }

          zipInputStream.closeEntry();
        }
      } finally {
        try {
          if ( zipInputStream != null ) {
            zipInputStream.close();
          }
        } catch ( IOException ioexception ) {
          this.logger.debug( ": Tried to close JarInputStream, but it was already closed.", ioexception );
        }
      }
    }

    private void processTgzArchive( List<String> capabilities, List<String> requirements ) throws IOException {
      TarArchiveInputStream tarGzInputStream = null;

      try {
        tarGzInputStream = new TarArchiveInputStream( new GzipCompressorInputStream( this.inputStream ) );

        TarArchiveEntry entry;
        while ( ( entry = tarGzInputStream.getNextTarEntry() ) != null ) {
          String name = entry.getName();

          if ( !entry.isDirectory() ) {
            processArchiveEntry( tarGzInputStream, name, capabilities, requirements );
          }
        }
      } finally {
        try {
          if ( tarGzInputStream != null ) {
            tarGzInputStream.close();
          }
        } catch ( IOException ioexception ) {
          this.logger.debug( ": Tried to close JarInputStream, but it was already closed.", ioexception );
        }
      }
    }

    private void processArchiveEntry( InputStream inputStream, String name, List<String> capabilities, List<String> requirements ) throws IOException {
      File temporarySourceFile = new File( this.absoluteTempResourcesPath.toAbsolutePath() + File.separator + FilenameUtils.separatorsToSystem( name ) );
      temporarySourceFile.getParentFile().mkdirs();

      BufferedOutputStream temporarySourceFileOutputStream = new BufferedOutputStream( new FileOutputStream( temporarySourceFile ) );

      byte[] bytes = new byte[ BYTES_BUFFER_SIZE ];
      int read;
      while ( ( read = inputStream.read( bytes ) ) != -1 ) {
        temporarySourceFileOutputStream.write( bytes, 0, read );
      }

      temporarySourceFileOutputStream.close();

      if ( FilenameUtils.getName( name ).equals( WebPackageURLConnection.PACKAGE_JSON ) ) {
        processPackageJson( temporarySourceFile, name, capabilities, requirements );
      }
    }

    private void processPackageJson( File temporarySourceFile, String name, List<String> capabilities, List<String> requirements ) throws FileNotFoundException {
      Map<String, Object> packageJson = parsePackageJson( new FileInputStream( temporarySourceFile ) );

      String moduleName = (String) packageJson.get( "name" );
      String moduleVersion = VersionParser.parseVersion( (String) packageJson.get( "version" ) ).toString();
      String root = name.replace( WebPackageURLConnection.PACKAGE_JSON, "" );
      if ( root.endsWith( "/" ) ) {
        root = root.substring( 0, root.length() - 1 );
      }

      capabilities.add( CAPABILITY_NAMESPACE + ";name=\"" + moduleName + "\";version:Version=\"" + moduleVersion + "\";root=\"/" + this.resourcesFolderName + "/" + root + "\"" );

      // we can't use required capabilities until all the platform is using capability based web packages
//      if ( packageJson.containsKey( "dependencies" ) ) {
//        HashMap<String, ?> deps = (HashMap<String, ?>) packageJson.get( "dependencies" );
//
//        final Set<String> depsKeySet = deps.keySet();
//        for ( String key : depsKeySet ) {
//          requirements.add( PentahoWebPackageBundleListener.CAPABILITY_NAMESPACE + ";filter:=\"(&(name=" + key + ")(version>=" + (String) deps.get( key ) + "))\"" );
//        }
//      }
    }

    public Map<String, Object> parsePackageJson( InputStream inputStream ) {
      try {
        InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
        BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

        return (Map<String, Object>) (new JSONParser()).parse( bufferedReader );
      } catch ( Exception e ) {
        throw new RuntimeException( "Error opening package.json", e );
      }
    }

    private void init() throws IOException {
      this.absoluteTempPath = Files.createTempDirectory( "PentahoWebPackageDeployer" );

      // those are needed until PAXWEB-1099 is in platform, to avoid the "same servlet name" issue
      this.resourcesFolderName = "pwp-" + UUID.randomUUID().toString();
      this.absoluteTempResourcesPath = Files.createDirectory( this.absoluteTempPath.resolve( this.resourcesFolderName ) );
    }

    private Manifest createManifest() {
      String name = FilenameUtils.getBaseName( this.url.getPath() );
      Version version = VersionParser.DEFAULT;

      // not essential, just trying to get a prettier name and version
      int i = name.lastIndexOf( '-' );
      if ( i != -1 ) {
        String possibleVersion = name.substring( i + 1 );
        if ( Character.isDigit( possibleVersion.charAt( 0 ) ) ) {
          version = VersionParser.parseVersion( possibleVersion );

          if ( !version.equals( VersionParser.DEFAULT ) ) {
            name = name.substring( 0, i );
          }
        }
      }

      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put( Attributes.Name.MANIFEST_VERSION, "1.0" );

      manifest.getMainAttributes()
          .put( new Attributes.Name( Constants.BUNDLE_MANIFESTVERSION ), "2" );

      manifest.getMainAttributes()
          .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webpackage-" + name );
      manifest.getMainAttributes()
          .put( new Attributes.Name( Constants.BUNDLE_VERSION ), version.toString() );

      return manifest;
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

    /**
     * Created by nbaker on 11/25/14.
     */
    static class VersionParser {
      private static Logger logger = LoggerFactory.getLogger( VersionParser.class );

      private static Version DEFAULT = new Version( 0, 0, 0 );
      private static Pattern VERSION_PAT = Pattern.compile( "([0-9]+)?(?:\\.([0-9]*)(?:\\.([0-9]*))?)?[\\.-]?(.*)" );
      private static Pattern CLASSIFIER_PAT = Pattern.compile( "[a-zA-Z0-9_\\-]+" );

      static Version parseVersion( String incomingVersion ) {
        if ( incomingVersion == null || incomingVersion.isEmpty() ) {
          return DEFAULT;
        }

        Matcher m = VERSION_PAT.matcher( incomingVersion );
        if ( !m.matches() ) {
          return DEFAULT;
        } else {
          String s_major = m.group( 1 );
          String s_minor = m.group( 2 );
          String s_patch = m.group( 3 );
          String classifier = m.group( 4 );
          Integer major = 0;
          Integer minor = 0;
          Integer patch = 0;

          if ( s_major != null && !s_major.isEmpty() ) {
            try {
              major = Integer.parseInt( s_major );
            } catch ( NumberFormatException e ) {
              logger.warn( "Major version part not an integer: " + s_major );
            }
          }

          if ( s_minor != null && !s_minor.isEmpty() ) {
            try {
              minor = Integer.parseInt( s_minor );
            } catch ( NumberFormatException e ) {
              logger.warn( "Minor version part not an integer: " + s_minor );
            }
          }

          if ( s_patch != null && !s_patch.isEmpty() ) {
            try {
              patch = Integer.parseInt( s_patch );
            } catch ( NumberFormatException e ) {
              logger.warn( "Patch version part not an integer: " + s_patch );
            }
          }

          if ( classifier != null ) {
            // classifiers cannot have a '.'
            classifier = classifier.replaceAll( "\\.", "_" );

            // Classifier characters must be in the following ranges a-zA-Z0-9_\-
            if ( !CLASSIFIER_PAT.matcher( classifier ).matches() ) {
              logger.warn( "Provided Classifier not valid for OSGI, ignoring" );
              classifier = null;
            }
          }

          if ( classifier != null ) {
            return new Version( major, minor, patch, classifier );
          } else {
            return new Version( major, minor, patch );
          }
        }
      }
    }
  }
}
