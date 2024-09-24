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
package org.pentaho.osgi.objecttunnel;

import io.reactivex.processors.PublishProcessor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
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
  private Map<String, TunnelSerializer> serializerMap = new HashMap<>();
  private List<Subscriber<? super TunneledInputObject>> subscribers = new ArrayList<>();
  private AtomicBoolean closed = new AtomicBoolean( false );
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private volatile int errorThreshold = 5;
  private volatile int errorCount = 0;
  private PublishProcessor<TunneledInputObject> publishProcessor = PublishProcessor.create();
  private int error_dampening_millis = 300;

  public TunnelInput( ObjectInputStream input, Map<Class, TunnelSerializer> rawSerializerMap ) {
    this.input = input;
    rawSerializerMap.entrySet().forEach( entry -> serializerMap.put( entry.getKey().getName(), entry.getValue() ) );
  }

  @Override public void close() throws Exception {
    closed.set( true );
    input.close();
    publishProcessor.onComplete();
  }

  int getErrorCount() {
    return errorCount;
  }

  public void setDampeningMillis( int millis ) {
    this.error_dampening_millis = millis;
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

  /**
   * Test Method
   *
   * @param clazz
   * @param func
   */
  void setDeserializer( Class clazz, Function<String, Object> func ) {
    serializerMap.put( clazz.getName(), new TunnelSerializer() {
      @Override public List<Class> getSupportedClasses() {
        return null;
      }

      @Override public String serialize( Object object ) {
        return null;
      }

      @Override public Object deserialize( String serializedString ) {
        return func.apply( serializedString );
      }
    } );
  }

  public void open() {
    if ( initialized.getAndSet( true ) ) {
      return;
    }
    Executors.newSingleThreadExecutor().submit( () -> {
      while ( !closed.get() && errorCount < errorThreshold ) {
        Exception capturedException = null;
        try {
          Object object = input.readObject();
          if ( object instanceof TunneledPayload ) {
            processTunneledPayload( (TunneledPayload) object );
          } else if ( object == TunnelMarker.END ) {
            close();
            return;
          } else if ( object instanceof String ) {
            // We would like to send a throwable, but this is not possible due to deserialization issues.
            String str = object.toString();
            if ( str.startsWith( "Error: " ) ) {
              publishProcessor.onError( new Exception( str ) {
                // Stacktrace from this point would be misleading, simply remove.
                @Override public synchronized Throwable fillInStackTrace() {
                  return this;
                }
              } );
            }
          } else {
            throw new IllegalStateException( "Unexpected object in stream: " + object.toString() );
          }
        } catch ( Exception e ) {
          // Something unexpected.
          errorCount++;
          // Delay the loop so we give time for socket issues to resolve
          try {
            Thread.sleep( error_dampening_millis );
          } catch ( InterruptedException ignored ) {
          }
          // Keep exception in case we want to throw
          capturedException = e;
        }
        if ( errorCount == errorThreshold ) {
          try {
            publishProcessor.onError( capturedException );
            close();
          } catch ( Exception ignored ) {
            //ignored
          }
        }
      }
    } );
  }

  private void processTunneledPayload( TunneledPayload payload ) {
    String type = payload.getType(); // Fully Qualified ClassName
    if ( serializerMap.containsKey( type ) ) {
      Object unmarshalled = serializerMap.get( type ).deserialize( payload.getObjectStr() );
      publishProcessor.onNext( new TunneledInputObject( type, unmarshalled ) );
    }
    errorCount = 0;
  }

  @Override public void subscribe( Subscriber<? super TunneledInputObject> s ) {
    if ( closed.get() ) {
      throw new IllegalStateException( "TunnelInput has been closed" );
    }
    publishProcessor.subscribe( s );
  }
}
