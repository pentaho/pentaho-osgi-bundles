/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer;

import org.pentaho.osgi.platform.plugin.deployer.impl.ExceptionSettable;

import java.io.IOException;
import java.io.PipedInputStream;

/**
 * Created by bryan on 8/26/14.
 */
public class ExceptionPipedInputStream extends PipedInputStream implements ExceptionSettable<Throwable> {
  private Throwable throwable = null;

  public ExceptionPipedInputStream( int pipeSize ) {
    super( pipeSize );
  }

  public int getPipeSize() {
    return buffer.length;
  }

  @Override public synchronized int read() throws IOException {
    if ( throwable != null ) {
      throw new IOException( "Got exception in producer thread.", throwable );
    }
    return super.read();
  }

  @Override public synchronized int read( byte[] b, int off, int len ) throws IOException {
    if ( throwable != null ) {
      throw new IOException( "Got exception in producer thread.", throwable );
    }
    return super.read( b, off, len );
  }

  @Override public synchronized void close() throws IOException {
    if ( throwable != null ) {
      throw new IOException( "Got exception in producer thread.", throwable );
    }
    super.close();
  }

  @Override public synchronized int read( byte[] b ) throws IOException {
    if ( throwable != null ) {
      throw new IOException( "Got exception in producer thread.", throwable );
    }
    return super.read( b );
  }

  @Override public void setException( Throwable exception ) {
    this.throwable = exception;
  }
}
