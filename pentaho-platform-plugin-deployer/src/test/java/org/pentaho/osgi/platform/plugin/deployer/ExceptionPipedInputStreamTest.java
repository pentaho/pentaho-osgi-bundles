/*
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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.plugin.deployer;

import org.junit.Test;

import java.io.IOException;
import java.io.PipedOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 8/27/14.
 */
public class ExceptionPipedInputStreamTest {
  @Test
  public void testConstructor() {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    assertEquals( 10, exceptionPipedInputStream.getPipeSize() );
  }

  @Test
  public void testReadNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read();
  }

  @Test( expected = IOException.class )
  public void testReadException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read();
  }

  @Test
  public void testReadByteArrayNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ] );
  }

  @Test( expected = IOException.class )
  public void testReadByteArrayException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ] );
  }

  @Test
  public void testReadByteArrayOffsetLengthNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ], 0, 1 );
  }

  @Test( expected = IOException.class )
  public void testReadByteArrayOffsetLengthException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ], 0, 1 );
  }

  @Test
  public void testCloseNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.close();
  }

  @Test( expected = IOException.class )
  public void testCloseException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.close();
  }
}
