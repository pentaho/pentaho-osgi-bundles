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
