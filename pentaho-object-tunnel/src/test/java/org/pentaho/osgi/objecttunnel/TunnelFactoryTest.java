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

package org.pentaho.osgi.objecttunnel;

import org.junit.Before;
import org.junit.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutput;
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
