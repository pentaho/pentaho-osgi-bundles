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

import org.junit.Before;
import org.junit.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nbaker on 2/14/17.
 */
public class TunnelFactoryTest {
  TunnelFactory factory;

  @Before
  public void setup() throws Exception {
    factory = new TunnelFactory( );

    factory.setSerializers( Collections.singletonList( new TunnelSerializer<UUID>() {
      @Override public List<Class> getSupportedClasses() {
        return Collections.singletonList( UUID.class );
      }

      @Override public String serialize( Object object ) {
        return object.toString();
      }

      @Override public UUID deserialize( String serializedString ) {
        return UUID.fromString( serializedString );
      }
    } ) );
  }

  @Test
  public void setSerializers() throws Exception {
    Map<Class, TunnelSerializer> serializerMap = factory.getSerializerMap();
    assertNotNull( serializerMap );
    assertTrue( serializerMap.containsKey( UUID.class ) );
  }

  @Test
  public void createOutput() throws Exception {
    ObjectOutputStream mock = mock( ObjectOutputStream.class );
    TunnelOutput output = factory.createOutput( mock );
    assertNotNull( output );
  }

  @Test
  public void createInput() throws Exception {
    ObjectInputStream mock = mock( ObjectInputStream.class );
    TunnelInput input = factory.createInput( mock );
    assertNotNull( input );
  }

}