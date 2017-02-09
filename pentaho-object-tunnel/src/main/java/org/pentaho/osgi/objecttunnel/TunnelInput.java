/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.osgi.objecttunnel;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Created by nbaker on 2/6/17.
 */
public class TunnelInput implements Publisher<TunneledInputObject>, AutoCloseable {

  private ObjectInputStream input;
  private List<Subscriber<? super TunneledInputObject>> subscribers = new ArrayList<>();
  private AtomicBoolean closed = new AtomicBoolean( false );
  private Map<String, Function<String, Object>> deserializeFunctions = new HashMap<>();
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private volatile int errorThreshold = 5;
  private volatile int errorCount = 0;

  public TunnelInput( ObjectInputStream input ) {
    this.input = input;
  }

  @Override public void close() throws Exception {
    closed.set( true );
    input.close();
    subscribers.forEach( Subscriber::onComplete );
  }

  public void setDeserializeFunctions(
    Map<String, Function<String, Object>> deserializeFunctions ) {
    this.deserializeFunctions.clear();
    this.deserializeFunctions.putAll( deserializeFunctions );
  }

  int getErrorCount() {
    return errorCount;
  }

  /**
   * Set the Error threshold for the TunnelInput. The error count is increased for every consequtive error from the
   * stream. If an object is successfully retrieved the error count is zero-ed out.
   * <p>
   * If the error count exceeds the threshold the Stream will be closed and the tunnel shut down.
   *
   * @param errorThreshold
   */
  public void setErrorThreshold( int errorThreshold ) {
    this.errorThreshold = errorThreshold;
  }

  public void open() {
    if ( initialized.getAndSet( true ) ) {
      return;
    }
    Executors.newSingleThreadExecutor().submit( () -> {
      while ( !closed.get() && errorCount < errorThreshold ) {
        try {
          TunneledPayload payload = (TunneledPayload) input.readObject();

          String type = payload.getType();
          if ( deserializeFunctions.containsKey( type ) ) {
            Object unmarshalled = deserializeFunctions.get( type ).apply( payload.getObjectStr() );
            subscribers.forEach( subscriber -> subscriber.onNext( new TunneledInputObject( type, unmarshalled ) ) );
          }
          errorCount = 0;

        } catch ( IOException e ) {
          errorCount++;
          subscribers.forEach( subscriber -> subscriber.onError( e ) );
        } catch ( ClassNotFoundException e ) {
          errorCount++;
          // We only deserialize one class, so this is unlikely.
          subscribers.forEach( subscriber -> subscriber.onError( e ) );
        } catch ( Exception e ) {
          errorCount++;
          // Something unexpected. Pass it on
          subscribers.forEach( subscriber -> subscriber.onError( e ) );
        }
        if ( errorCount == errorThreshold ) {
          try {
            close();
          } catch ( Exception ignored ) {
            //ignored
          }
        }
      }
    } );
  }

  @Override public void subscribe( Subscriber<? super TunneledInputObject> s ) {
    if ( closed.get() ) {
      throw new IllegalStateException( "TunnelInput has been closed" );
    }
    subscribers.add( s );
  }
}
