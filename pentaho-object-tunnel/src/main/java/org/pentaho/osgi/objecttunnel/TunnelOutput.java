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
  private List<Subscriber<? super TunneledInputObject>> subscribers = new ArrayList<>();
  private AtomicBoolean closed = new AtomicBoolean( false );
  private Map<String, Function<Object, String>> serializeFunctions = new HashMap<>();
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private volatile int errorThreshold = 5;

  public TunnelOutput( ObjectOutputStream output ) {
    this.output = output;
  }

  @Override public void close() throws Exception {
    closed.set( true );
    output.close();
  }

  public void setSerializeFunctions(
    Map<String, Function<Object, String>> serializeFunctions ) {
    this.serializeFunctions.clear();
    this.serializeFunctions.putAll( serializeFunctions );
  }

  public void writeObject( String type, Object object ) throws IOException {
    if ( !serializeFunctions.containsKey( type ) ) {
      throw new IllegalArgumentException( "No Serialize Function for given type: " + type );
    }
    if ( object == null ) {
      throw new NullPointerException( "Given TunnelInput Object is null" );
    }
    try {
      Stream.of( serializeFunctions.get( type ) ).map( s -> s.apply( object ) )
        .map( o -> new TunneledPayload( type, o ) ).forEach(
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
