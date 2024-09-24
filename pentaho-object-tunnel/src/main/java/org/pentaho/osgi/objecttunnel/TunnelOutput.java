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

import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by nbaker on 2/6/17.
 */
public class TunnelOutput implements AutoCloseable {

  private ObjectOutputStream output;
  private Map<Class, TunnelSerializer> serializerMap = new HashMap<>(  );
  private List<Subscriber<? super TunneledInputObject>> subscribers = new ArrayList<>();
  private AtomicBoolean closed = new AtomicBoolean( false );
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private volatile int errorThreshold = 5;

  public TunnelOutput( ObjectOutputStream output, Map<Class, TunnelSerializer> serializerMap ) {
    this.output = output;
    this.serializerMap.putAll( serializerMap );
  }

  @Override public void close() throws Exception {
    closed.set( true );
    output.close();
  }

  void setSerializer( Class clazz, Function<Object, String> serializer ) {
    serializerMap.put( clazz, new TunnelSerializer() {
      @Override public List<Class> getSupportedClasses() {
        return null;
      }

      @Override public String serialize( Object object ) {
        return serializer.apply( object );
      }

      @Override public Object deserialize( String serializedString ) {
        return null;
      }
    } );
  }

  public void writeObject( Object object ) throws IOException {
    Class<?> type = object.getClass();
    if ( !serializerMap.containsKey( type ) ) {
      throw new IllegalArgumentException( "No Serialize Function for given type: " + type );
    }
    if ( object == null ) {
      throw new NullPointerException( "Given TunnelInput Object is null" );
    }
    try {
      Stream.of( serializerMap.get( type ) ).map( s -> s.serialize( object ) )
        .map( o -> new TunneledPayload( type.getName(), o ) ).forEach(
          o -> {
            try {
              output.writeObject( o );
            } catch ( IOException e ) {
              throw new ExceptionWrapper( e );
            }
          } );
    } catch ( ExceptionWrapper wrapper ) {
      throw wrapper.ioe;
    }

  }

  private static class ExceptionWrapper extends RuntimeException {
    IOException ioe;
    ExceptionWrapper( IOException e ) {
      super( e );
      ioe = e;
    }
  }


}
