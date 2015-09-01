package org.pentaho.platform.config;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.ram.RamFileObject;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by nbaker on 8/7/15.
 */
public class ConfigVfsProvider extends UrlFileProvider {
  private File root = new File( "src/test/resources" );

  @Override
  public synchronized FileObject findFile( FileObject baseFile, String uri, FileSystemOptions fileSystemOptions )
      throws FileSystemException {
    FileObject internal  = super.findFile( null, new File("src/test/resources/internal").toURI().toString(), null );
    FileObject custom  = super.findFile( null, new File("src/test/resources/custom").toURI().toString(), null );

    URI incoming = null;
    try {
      incoming = new URI( uri );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
    FileObject customVersion = super.findFile( custom, custom.getURL()+incoming.getPath(), fileSystemOptions );
    FileObject internalVersion = super.findFile( internal, internal.getURL()+incoming.getPath(), fileSystemOptions );
    if( customVersion.exists()){

      if( internalVersion.exists() ){
        if( internalVersion.getURL().getFile().endsWith( ".properties" )){
          Properties props = new Properties(  );
          try {
            props.load( internalVersion.getContent().getInputStream() );
          } catch ( IOException e ) {
            e.printStackTrace();
          }

          try {
            props.load( customVersion.getContent().getInputStream() );
          } catch ( IOException e ) {
            e.printStackTrace();
          }
          ByteArrayOutputStream out = new ByteArrayOutputStream(  );
          try {
            props.store( out, "merged" );
          } catch ( IOException e ) {
            e.printStackTrace();
          }
          final byte[] bytes = out.toByteArray();

          FileObject fileObject = new AbstractFileObject((AbstractFileName) customVersion.getName(), (AbstractFileSystem) customVersion.getFileSystem()){
            @Override protected FileType doGetType() throws Exception {
              return FileType.FILE;
            }

            @Override protected String[] doListChildren() throws Exception {
              return new String[ 0 ];
            }

            @Override protected long doGetContentSize() throws Exception {
              return bytes.length;
            }

            @Override protected InputStream doGetInputStream() throws Exception {
              return new ByteArrayInputStream( bytes );
            }
          };
          return fileObject;
        }
      }
      return customVersion;
    } else {
      return internalVersion;
    }
  }
}
