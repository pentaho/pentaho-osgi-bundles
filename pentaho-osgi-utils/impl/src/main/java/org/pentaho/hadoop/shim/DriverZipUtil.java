package org.pentaho.hadoop.shim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DriverZipUtil {

  private static Logger logger = LoggerFactory.getLogger( DriverManager.class );

  public static void unzipFile( String zipFileName, String destDirectoryName ) throws Exception {

    byte[] buffer = new byte[ 1024 ];
    ZipInputStream zis = new ZipInputStream( new FileInputStream( zipFileName ) );
    ZipEntry zipEntry = zis.getNextEntry();
    File destDirectoryFile = new File( destDirectoryName );

    if ( zipEntryExists( destDirectoryFile, zipEntry ) ) {
      logger.info( "Driver is already installed." );
      return;
    }

    while ( zipEntry != null ) {
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
      zipEntry = zis.getNextEntry();
    }

    zis.closeEntry();
    zis.close();
  }

  private static File newFile( File destinationDir, ZipEntry zipEntry ) throws IOException {
    File destFile = new File( destinationDir, zipEntry.getName() );

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
}
