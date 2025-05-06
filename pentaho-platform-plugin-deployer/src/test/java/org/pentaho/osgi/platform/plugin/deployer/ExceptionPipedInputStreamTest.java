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
  public void testConstructor() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    assertEquals( 10, exceptionPipedInputStream.getPipeSize() );
    exceptionPipedInputStream.close();
  }

  @Test
  public void testReadNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read();
    pipedOutputStream.close();
  }

  @Test( expected = IOException.class )
  public void testReadException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read();
    pipedOutputStream.close();
  }

  @Test
  public void testReadByteArrayNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ] );
    pipedOutputStream.close();
  }

  @Test( expected = IOException.class )
  public void testReadByteArrayException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ] );
    pipedOutputStream.close();
  }

  @Test
  public void testReadByteArrayOffsetLengthNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ], 0, 1 );
    pipedOutputStream.close();
  }

  @Test( expected = IOException.class )
  public void testReadByteArrayOffsetLengthException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.read( new byte[ 1 ], 0, 1 );
    pipedOutputStream.close();
  }

  @Test
  public void testCloseNoException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.close();
    pipedOutputStream.close();
  }

  @Test( expected = IOException.class )
  public void testCloseException() throws IOException {
    ExceptionPipedInputStream exceptionPipedInputStream = new ExceptionPipedInputStream( 10 );
    PipedOutputStream pipedOutputStream = new PipedOutputStream( exceptionPipedInputStream );
    exceptionPipedInputStream.setException( new IOException( ) );
    pipedOutputStream.write( 1 );
    exceptionPipedInputStream.close();
    pipedOutputStream.close();
  }
}
