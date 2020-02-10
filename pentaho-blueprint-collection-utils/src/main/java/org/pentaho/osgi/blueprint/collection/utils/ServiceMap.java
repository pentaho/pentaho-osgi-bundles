/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.osgi.blueprint.collection.utils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;

/**
 * Created by tkafalas on 6/19/2017
 */
public class ServiceMap<T> {

  private final Map<String, MapItem<T>> map = new ConcurrentHashMap<>();
  public static final String SERVICE_KEY_PROPERTY = "serviceMapKey";

  public T getItem( String key ) {
    if ( key == null ) {
      return null;
    }
    synchronized ( map ) {
      return map.get( key ) != null ? map.get( key ).getItem() : null;
    }
  }

  public void itemAdded( T item, @SuppressWarnings( "rawtypes" ) Map config ) {
    if ( item == null ) {
      return;
    }
    String key = getKeyFromConfig( config );

    synchronized ( map ) {
      map.put( key, new MapItem<T>( key, item ) );
    }
  }

  public void itemRemoved( T item, @SuppressWarnings( "rawtypes" ) Map config ) {
    //item is ignored.  It is in the signature because OSGI requires it.
    synchronized ( map ) {
      try {
        map.remove( getKeyFromConfig( config ) );
      } catch ( IllegalArgumentException e ) {
        // Do nothing, Defensive code may try remove something that is not registered or already removed.
      }
    }
  }

  @VisibleForTesting
  String getToString( String key, T item ) {
    return new MapItem<T>( key, item ).toString();
  }

  protected Map<String, T> getMap() {
    synchronized ( map ) {
      Map<String, T> m = map.values().stream().collect( Collectors.toMap( MapItem::getKey, MapItem::getItem ) );
      return Collections.unmodifiableMap( m );
    }
  }

  private String getKeyFromConfig( @SuppressWarnings( "rawtypes" ) Map config ) {
    String key = null;
    if ( config != null ) {
      key = (String) config.get( SERVICE_KEY_PROPERTY );
      if ( key != null ) {
        return key;
      }
    }
    throw new IllegalArgumentException( SERVICE_KEY_PROPERTY + " is null or undefined" );
  }

  public class MapItem<T> {
    final String key;
    final T item;

    MapItem( String key, T item ) {
      this.key = key;
      this.item = item;
    }

    String getKey() {
      return key;
    }

    T getItem() {
      return item;
    }

    @Override
    public String toString() {
      return "(" + key + ") " + item;
    }
  }
}
