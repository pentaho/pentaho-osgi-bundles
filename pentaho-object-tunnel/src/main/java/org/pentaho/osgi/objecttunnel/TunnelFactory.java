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

  public TunnelFactory() {

  }

  public void setSerializers( List<TunnelSerializer<?>> serializers ) {
    if ( serializers != null ) {
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

  public void addSerializer( TunnelSerializer<?> serializer ) {
    serializers.add( serializer );
    populateSerializerMap();
  }

  public void removeSerializer( TunnelSerializer<?> serializer ) {
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
