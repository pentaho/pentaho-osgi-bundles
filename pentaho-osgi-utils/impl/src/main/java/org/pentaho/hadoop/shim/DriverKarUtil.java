package org.pentaho.hadoop.shim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DriverKarUtil {

  private static Logger logger = LoggerFactory.getLogger( DriverManager.class );

  public static void processKarFile( String karFileName, String karDirectory, String destDirectoryName ) throws Exception {

    byte[] buffer = new byte[ 1024 ];
    ZipInputStream zis = new ZipInputStream( new FileInputStream( karDirectory + "/" + karFileName ) );
    ZipEntry zipEntry = zis.getNextEntry();
    File destDirectoryFile = new File( destDirectoryName );

    if ( zipEntryExists( destDirectoryFile, zipEntry ) ) {
      logger.info( "Driver is already installed." );
      return;
    }

    while ( zipEntry != null ) {
      if ( zipEntryIsTheDriver( karFileName, zipEntry ) ) {
        String driverName = getSimpleName( zipEntry ).replaceAll( ".jar", "" );
        logger.info( driverName + " is the driver." );
        processDriver( zis, destDirectoryName, driverName );
      }
      zipEntry = zis.getNextEntry();
    }

    zis.closeEntry();
    zis.close();
  }

  private static void processDriver( ZipInputStream driverZipIS, String destDirectoryName, String driverName ) throws Exception {
    byte[] buffer = new byte[ 1024 ];
    ZipInputStream zis = new ZipInputStream( driverZipIS );
    ZipEntry zipEntry = zis.getNextEntry();
    File destDirectoryFile = new File( destDirectoryName + "/" + driverName + "/lib" );
    File driverJarFile = new File( destDirectoryName + "/" + driverName + "/" + driverName + ".jar" );
    driverJarFile.getParentFile().mkdirs();
    ZipOutputStream driverJarOS = new ZipOutputStream( new FileOutputStream( driverJarFile ) );

    while ( zipEntry != null ) {
      if ( zipEntry.getName().endsWith( ".jar" ) ) {
        File newFile = newFile( destDirectoryFile, zipEntry );
        if ( zipEntry.isDirectory() ) {
          if ( !newFile.isDirectory() && !newFile.mkdirs() ) {
            throw new IOException( "Failed to create directory " + newFile );
          }
        } else {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if ( !parent.isDirectory() && !parent.mkdirs() ) {
            throw new IOException( "Failed to create directory " + parent );
          }

          // write file content
          FileOutputStream fos = new FileOutputStream( newFile );
          int len;
          while ( ( len = zis.read( buffer ) ) > 0 ) {
            fos.write( buffer, 0, len );
          }
          fos.close();
        }
      } else {
        driverJarOS.putNextEntry( new ZipEntry( zipEntry.getName() ) );
        int len;
        while ( ( len = zis.read( buffer ) ) > 0 ) {
          driverJarOS.write( buffer, 0, len );
        }
      }
      zipEntry = zis.getNextEntry();
    }
    driverJarOS.close();
  }

  private static File newFile( File destinationDir, ZipEntry zipEntry ) throws IOException {
    File destFile = new File( destinationDir, getSimpleName( zipEntry ) );

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if ( !destFilePath.startsWith( destDirPath + File.separator ) ) {
      throw new IOException( "Entry is outside of the target dir: " + zipEntry.getName() );
    }

    return destFile;
  }

  private static boolean zipEntryExists( File destinationDir, ZipEntry zipEntry ) throws IOException {
    File destFile = new File( destinationDir, zipEntry.getName() );
    return destFile.exists();
  }

  private static boolean zipEntryIsTheDriver( String karFileName, ZipEntry zipEntry ) {
    String zipEntryName = getSimpleName( zipEntry );
    return cleanName( karFileName ).equals( cleanName( zipEntryName ) );
  }

  private static String cleanName( String fileName ) {
    return fileName.replaceAll( "kar", "" ).replaceAll( "driver", "" ).replaceAll( "jar", "" );
  }

  private static String getSimpleName( ZipEntry zipEntry ) {
    String zipEntryName = zipEntry.getName();
    String simpleName = Paths.get( zipEntryName ).getFileName().toString();
    return simpleName;
  }
}
