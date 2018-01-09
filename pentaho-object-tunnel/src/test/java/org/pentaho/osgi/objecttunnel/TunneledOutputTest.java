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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 2/7/17.
 */
public class TunneledOutputTest {

  private Map<Class, TunnelSerializer> serializerMap = new HashMap<>();
  @Test
  public void testOutput() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream( bos );
    TunnelOutput output = new TunnelOutput( outputStream, serializerMap );
    output.setSerializer( UUID.class, o -> {
      return o.toString();
    } );
    UUID uuid = UUID.randomUUID();
    output.writeObject( uuid );

    byte[] bytes = bos.toByteArray();
    ObjectInputStream inputStream = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
    TunneledPayload out = (TunneledPayload) inputStream.readObject();

    assertEquals( uuid.toString(), out.getObjectStr() );
  }

  @Test
  public void checkClose() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream, serializerMap );
    output.close();
    verify( mockOutputStream, times( 1 ) ).close();
  }

  @Test( expected = IllegalArgumentException.class )
  public void testNoFactory() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream, serializerMap );
    output.writeObject( "bar" );
  }

  @Test( expected = NullPointerException.class )
  public void testConditions() throws Exception {

    ObjectOutputStream mockOutputStream = mock( ObjectOutputStream.class );
    TunnelOutput output = new TunnelOutput( mockOutputStream, serializerMap );
    output.setSerializer( String.class, o -> "" );
    output.writeObject(  null );
  }

  @Test( expected = IOException.class )
  public void testErrorOnWrite() throws Exception {

    ObjectOutputStream outputStream = new ObjectOutputStream() {
      @Override protected void writeObjectOverride( Object obj ) throws IOException {
        throw new IOException( "I am IO" );
      }
    };
    TunnelOutput output = new TunnelOutput( outputStream, serializerMap );
    output.setSerializer( String.class, o -> "" );
    output.writeObject( "bar" );
  }
}