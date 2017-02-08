package org.pentaho.osgi.objecttunnel;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 2/7/17.
 */
public class TunneledOutputTest {

  @Test
  public void testOutput() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream( bos );
    TunnelOutput output = new TunnelOutput( outputStream );
    output.setSerializeFunctions( Collections.singletonMap( "UUID", o -> {
      return o.toString();
    } ) );
    UUID uuid = UUID.randomUUID();
    output.writeObject( "UUID", uuid );

    byte[] bytes = bos.toByteArray();
    ObjectInputStream inputStream = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
    TunneledPayload out = (TunneledPayload) inputStream.readObject();

    assertEquals( uuid.toString(), out.getObjectStr() );
  }

  @Test
  public void checkClose() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream );
    output.close();
    verify( mockOutputStream, times( 1 ) ).close();
  }

  @Test( expected = IllegalArgumentException.class )
  public void testNoFactory() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream );
    output.writeObject( "will not be found", "bar" );
  }

  @Test( expected = NullPointerException.class )
  public void testConditions() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream );
    output.setSerializeFunctions( Collections.singletonMap( "foo", o -> "" ) );
    output.writeObject( "foo", null );
  }

  @Test( expected = IOException.class )
  public void testErrorOnWrite() throws Exception {

    ObjectOutputStream outputStream = new ObjectOutputStream() {
      @Override protected void writeObjectOverride( Object obj ) throws IOException {
        throw new IOException( "I am IO" );
      }
    };
    TunnelOutput output = new TunnelOutput( outputStream );
    output.setSerializeFunctions( Collections.singletonMap( "foo", o -> "" ) );
    output.writeObject( "foo", "bar" );
  }
}