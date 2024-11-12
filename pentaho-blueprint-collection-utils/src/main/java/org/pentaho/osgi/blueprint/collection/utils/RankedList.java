/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.blueprint.collection.utils;

import com.google.common.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by bryan on 3/29/16.
 */
public class RankedList<T> {
  public static final String SERVICE_RANKING = "service.ranking";
  private final Comparator<T> comparator;
  private final Set<RankedItem> set;
  private List<T> list;

  public RankedList( Comparator<T> comparator ) {
    this.comparator = comparator;
    this.set = new TreeSet<>();
    this.list = null;
  }

  public void itemAdded( T item, Map config ) {
    if ( item == null ) {
      return;
    }
    Object ranking = null;
    if ( config != null ) {
      ranking = config.get( SERVICE_RANKING );
    }
    if ( ranking == null ) {
      ranking = 0;
    }
    if ( !( ranking instanceof Number ) ) {
      ranking = Integer.parseInt( ranking.toString() );
    }

    synchronized ( set ) {
      set.add( new RankedItem( ( (Number) ranking ).intValue(), item ) );
      list = null;
    }
  }

  public void itemRemoved( T item ) {
    if ( item == null ) {
      return;
    }

    synchronized ( set ) {
      set.removeAll(
        set.stream().filter( rankedItem -> rankedItem.item.equals( item ) ).collect( Collectors.toList() ) );
      list = null;
    }
  }

  protected List<T> getList() {
    synchronized ( set ) {
      if ( list == null ) {
        list = Collections.unmodifiableList( set.stream().map( RankedItem::getItem ).collect( Collectors.toList() ) );
      }
      return list;
    }
  }

  @VisibleForTesting
  String getToString( int rank, T item ) {
    return new RankedItem( rank, item ).toString();
  }

  private class RankedItem implements Comparable<RankedItem> {
    final int rank;
    final T item;

    RankedItem( int rank, T item ) {
      this.rank = rank;
      this.item = item;
    }

    int getRank() {
      return rank;
    }

    T getItem() {
      return item;
    }

    @Override public String toString() {
      return "(" + rank + ") " + item;
    }

    @Override public int compareTo( RankedItem o ) {
      return Comparator
        .comparingInt( RankedItem::getRank ).reversed()
        .thenComparing( RankedItem::getItem, comparator )
        .compare( this, o );
    }
  }
}
