/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
