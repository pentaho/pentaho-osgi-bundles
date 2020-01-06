/*!
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
* Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
*/

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
