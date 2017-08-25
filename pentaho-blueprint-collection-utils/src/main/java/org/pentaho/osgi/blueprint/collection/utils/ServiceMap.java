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
package org.pentaho.osgi.blueprint.collection.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;

/**
 * Created by tkafalas on 6/19/2017
 */
public class ServiceMap<T> {

  private final Map<String, MapItem<T>> map = new HashMap<String, MapItem<T>>();
  public static final String SERVICE_KEY_PROPERTY = "serviceMapKey";

  public T getItem( String key ) {
    return map.get( key ) != null ? map.get( key ).getItem() : null;
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
