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
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Locale;
import java.util.Map;

public class MetadataToMondrianVfsFileContent implements FileContent {

  private MetadataToMondrianVfsFileObject fileObject;

  private InputStream inputStream = null;

  private boolean isOpen = false;

  public MetadataToMondrianVfsFileContent( final MetadataToMondrianVfsFileObject fileObject ) {
    super();
    this.fileObject = fileObject;
  }

  public FileObject getFile() {
    return fileObject;
  }

  public long getSize() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public long getLastModifiedTime() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void setLastModifiedTime( final long arg0 ) throws FileSystemException {
    // not needed for our usage

  }

  public boolean hasAttribute( final String attrName ) {
    return false;
  }

  public void removeAttribute( final String attrName ) {
  }

  public Map getAttributes() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public String[] getAttributeNames() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public Object getAttribute( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void setAttribute( final String arg0, final Object arg1 ) throws FileSystemException {
    // not needed for our usage

  }

  public Certificate[] getCertificates() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public InputStream getInputStream() throws FileSystemException {

    try {
      // read in stream, generate mondrian model, write out stream.
      XmiParser parser = new XmiParser();
      InputStream inStream;
      File file = new File( fileObject.getFileRef() );
      if ( file.exists() ) {
        inStream = new FileInputStream( file );
      } else {
        inStream = new URL( fileObject.getFileRef() ).openStream();
      }
      Domain domain = parser.parseXmi( inStream );
      String locale = Locale.getDefault().toString();
      if ( domain.getLocales().size() > 0 ) {
        locale = domain.getLocales().get( 0 ).getCode();
      }

      if ( domain.getLogicalModels().size() == 0 ) {
        throw new Exception( "Domain " + fileObject.getFileRef() + " does not contain model." );
      }

      LogicalModel lModel = domain.getLogicalModels().get( 0 );
      if ( domain.getLogicalModels().size() > 1 ) {
        lModel = domain.getLogicalModels().get( 1 );
      }
      MondrianModelExporter exporter = new MondrianModelExporter( lModel, locale );
      String mondrianSchema = exporter.createMondrianModelXML();

      inputStream = new ByteArrayInputStream( mondrianSchema.getBytes() );
    } catch ( Exception e ) {
      throw new FileSystemException( e.getLocalizedMessage(), e );
    }
    isOpen = true;
    return inputStream;
  }

  public OutputStream getOutputStream() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public RandomAccessContent getRandomAccessContent( final RandomAccessMode arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public OutputStream getOutputStream( final boolean arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void close() throws FileSystemException {

    if ( !isOpen ) {
      return;
    }
    if ( inputStream != null ) {
      try {
        inputStream.close();
      } catch ( Exception e ) {
        // not much we can do here
      }
    }
    isOpen = false;
    fileObject.close();
  }

  public FileContentInfo getContentInfo() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public boolean isOpen() {
    // not needed for our usage
    return isOpen;
  }

  @Override public long write( FileContent fileContent ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( FileObject fileObject ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream outputStream ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream outputStream, int i ) throws IOException {
    throw new UnsupportedOperationException();
  }

}
