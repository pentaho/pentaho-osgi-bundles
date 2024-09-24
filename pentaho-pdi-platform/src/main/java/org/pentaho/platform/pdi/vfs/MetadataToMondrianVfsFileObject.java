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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.pdi.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.AbstractFileName;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class MetadataToMondrianVfsFileObject implements FileObject {

  @Override public int compareTo( FileObject o ) {
    throw new UnsupportedOperationException();
  }

  @Override public Iterator<FileObject> iterator() {
    throw new UnsupportedOperationException();
  }

  public class MetadataToMondrianVfsFileName extends AbstractFileName {

    public MetadataToMondrianVfsFileName( final String absPath, final FileType type ) {
      super( "mtm", absPath, type ); //$NON-NLS-1$
    }

    @Override protected void appendRootUri( StringBuilder stringBuilder, boolean b ) {

    }

    @Override
    public FileName createName( final String absPath, final FileType fileType ) {

      FileName name = new MetadataToMondrianVfsFileName( absPath, fileType );
      return name;
    }

  }

  private String fileRef;

  private FileContent content = null;

  private FileName name;

  private FileType type;

  public MetadataToMondrianVfsFileObject( final String fileRef ) {
    this.fileRef = fileRef;
    // try to guess the file type
    type = FileType.FILE;
    name = new MetadataToMondrianVfsFileName( fileRef, type );

  }

  public MetadataToMondrianVfsFileObject( final String fileRef, final FileType type ) {
    this.fileRef = fileRef;
    this.type = type;
    name = new MetadataToMondrianVfsFileName( fileRef, type );

  }

  public String getFileRef() {
    return fileRef;
  }

  public FileName getName() {
    return name;
  }

  public URL getURL() throws FileSystemException {
    URL url = null;
    try {
      url = new URL( "mtm:/" + fileRef ); //$NON-NLS-1$
    } catch ( Exception e ) {
      // ignore
    }
    return url;
  }

  public boolean exists() throws FileSystemException {
    return fileRef.startsWith( "http" ) || new File( fileRef ).exists();
  }

  public boolean isHidden() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public boolean isReadable() throws FileSystemException {
    // not needed for our usage
    return exists();
  }

  public boolean isWriteable() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public FileType getType() throws FileSystemException {
    return type;
  }

  public FileObject getParent() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  @Override public String getPublicURIString() {
    throw new UnsupportedOperationException();
  }

  public FileSystem getFileSystem() {
    // not needed for our usage
    return null;
  }

  public FileObject[] getChildren() throws FileSystemException {
    return null;
  }

  public FileObject getChild( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile( final String arg0, final NameScope arg1 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  @Override public boolean setExecutable( boolean b, boolean b1 ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean setReadable( boolean b, boolean b1 ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean setWritable( boolean b, boolean b1 ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public FileObject resolveFile( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject[] findFiles( final FileSelector arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void findFiles( final FileSelector arg0, final boolean arg1, final List arg2 ) throws FileSystemException {
    // not needed for our usage
  }

  public boolean delete() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public int delete( final FileSelector arg0 ) throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  @Override public int deleteAll() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public void createFolder() throws FileSystemException {
    // not needed for our usage

  }

  public void createFile() throws FileSystemException {
    // not needed for our usage

  }

  public void copyFrom( final FileObject arg0, final FileSelector arg1 ) throws FileSystemException {
    // not needed for our usage

  }

  public void moveTo( final FileObject arg0 ) throws FileSystemException {
    // not needed for our usage
  }

  public boolean canRenameTo( final FileObject arg0 ) {
    // not needed for our usage
    return false;
  }

  public FileContent getContent() throws FileSystemException {
    content = new MetadataToMondrianVfsFileContent( this );
    return content;
  }

  public void close() throws FileSystemException {
    if ( content != null ) {
      content.close();
      content = null;
    }
  }

  public void refresh() throws FileSystemException {
    // not needed for our usage
  }

  public boolean isAttached() {
    // not needed for our usage
    return false;
  }

  public boolean isContentOpen() {
    return ( content != null ) && content.isOpen();
  }

  @Override public boolean isExecutable() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean isFile() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean isFolder() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
