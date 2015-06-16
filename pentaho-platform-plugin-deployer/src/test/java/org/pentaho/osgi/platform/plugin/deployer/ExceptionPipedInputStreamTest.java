/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
