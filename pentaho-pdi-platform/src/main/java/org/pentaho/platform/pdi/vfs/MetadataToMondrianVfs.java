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


package org.pentaho.platform.pdi.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;

import java.util.Collection;

/**
 * this VFS provider converts a metadata model on the fly to a mondrian file, so we don't need to store tmp mondrian
 * files
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class MetadataToMondrianVfs extends AbstractFileProvider {

  public MetadataToMondrianVfs() {
    super();
  }

  public FileObject findFile( final FileObject baseFile, final String uri, final FileSystemOptions arg2 )
    throws FileSystemException {

    // for now assume that all URIs are absolute and we don't handle compound URIs
    if ( uri != null ) {
      // this is a fully qualified file path
      int pos = uri.indexOf( ':' );
      String filePath = uri.substring( pos + 1 );
      MetadataToMondrianVfsFileObject fileInfo = new MetadataToMondrianVfsFileObject( filePath );
      return fileInfo;
    }
    return null;
  }

  @Override
  public FileObject createFileSystem( final String arg0, final FileObject arg1, final FileSystemOptions arg2 )
    throws FileSystemException {
    // not needed for our usage
    return null;
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    // not needed for our usage
    return null;
  }

  @Override
  public Collection getCapabilities() {
    // not needed for our usage
    return null;
  }

  @Override
  public FileName parseUri( final FileName arg0, final String arg1 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
