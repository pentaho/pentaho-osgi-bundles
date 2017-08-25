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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for Tunnels. The main use of this factory it to serve as the component which contains all TunnelSerializer
 * instances registered in the system.
 *
 * Created by nbaker on 2/14/17.
 */
public class TunnelFactory {

  private Map<Class, TunnelSerializer> serializerMap = new HashMap<>(  );
  private List<TunnelSerializer<?>> serializers = new ArrayList<>(  );

  public TunnelFactory(){

  }

  public void setSerializers( List<TunnelSerializer<?>> serializers ) {
    if( serializers != null ) {
      this.serializers.addAll( serializers );
    }
    populateSerializerMap();
  }

  public void populateSerializerMap( ) {
    serializerMap.clear();
    for ( TunnelSerializer<?> serializer : serializers ) {
      List<Class> supportedClasses = serializer.getSupportedClasses();
      for ( Class supportedClass : supportedClasses ) {
        serializerMap.put( supportedClass, serializer );
      }
    }
    //    serializers.forEach( s -> s.getSupportedClasses().stream( clz -> serializerMap.put( clz, s) ) );
  }

  public void addSerializer( TunnelSerializer<?> serializer ){
    serializers.add( serializer );
    populateSerializerMap();
  }

  public void removeSerializer( TunnelSerializer<?> serializer ){
    serializers.remove( serializer );
    populateSerializerMap();
  }

  /**
   * Test only.
   * @return
   */
  Map<Class, TunnelSerializer> getSerializerMap() {
    return serializerMap;
  }

  public TunnelSerializer getSerializer( Class clazz ) {
    return serializerMap.get( clazz );
  }

  public TunnelOutput createOutput( ObjectOutputStream outputStream ) {
    return new TunnelOutput( outputStream, serializerMap );
  }

  public TunnelInput createInput( ObjectInputStream objectInputStream ) {
    return new TunnelInput( objectInputStream, serializerMap );
  }

}
