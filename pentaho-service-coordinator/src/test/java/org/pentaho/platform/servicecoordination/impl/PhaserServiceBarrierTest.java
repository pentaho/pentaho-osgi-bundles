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

package org.pentaho.platform.servicecoordination.impl;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

public class PhaserServiceBarrierTest {

  PhaserServiceBarrier barrier;

  @Before
  public void setup(){
    barrier = new PhaserServiceBarrier();
  }


  @Test
  public void testHoldAndRelease() throws Exception {
    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        locked.set( false );
      }
    } );
    barrier.hold();
    t1.start();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( t1.getState() == Thread.State.WAITING );
    barrier.release();
    while( t1.isAlive() ){
      Thread.sleep( 10 );
    }

    assertFalse( locked.get() );

  }

  @Test
  public void testAwaitWithNoHold() throws Exception {
    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );
    final AtomicBoolean secondlock = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        locked.set( true );
        barrier.hold();
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        secondlock.set( true );
      }
    } );
    t1.start();
    while( locked.get() == false ){
      Thread.sleep( 10 );
    }
    barrier.release();
    while( t1.isAlive() ){
      Thread.sleep( 10 );
    }

    assertTrue( locked.get() );
    assertTrue( secondlock.get() );

  }

  @Test
  public void testGetHoldCount() throws Exception {
    assertEquals( 0, barrier.getHoldCount() );
    barrier.hold();
    barrier.hold();
    assertEquals( 2, barrier.getHoldCount() );
    barrier.release();
    assertEquals( 1, barrier.getHoldCount() );

  }

  @Test
  public void testIsAvailable() throws Exception {
    assertTrue( barrier.isAvailable() );
    barrier.hold();
    barrier.hold();
    assertFalse( barrier.isAvailable() );
    barrier.release();
    assertFalse( barrier.isAvailable() );
    barrier.release();
    assertTrue( barrier.isAvailable() );
  }

  @Test
  public void testTerminate() throws Exception {

    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );
    final AtomicBoolean threwException = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        barrier.hold();
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          threwException.set( true );
          return;
        }
        locked.set( false );
      }
    } );

    t1.start();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( t1.getState() == Thread.State.WAITING );
    barrier.terminate();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( threwException.get() );
    assertTrue( barrier.isTerminated() );
  }

}
