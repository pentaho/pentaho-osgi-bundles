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
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings( "unchecked" )
public class CountdownLatchServiceLifecycleManagerTest {

  BaseCountdownLatchLifecycleManager manager;

  private static class TestEvent {

  }
  static TestEvent DUMMY_EVENT = new TestEvent();

  @Before
  public void setup() {
    manager = new BaseCountdownLatchLifecycleManager<TestEvent>(){
      @Override protected TestEvent getNotificationObject() {
        return DUMMY_EVENT;
      }
    };


  }

  @Test
  public void testGetPhase() throws Exception {
    assertEquals( 0, manager.getPhase() );
    manager.advance();
    assertEquals( 1, manager.getPhase() );
    manager.retreat();
    assertEquals( 0, manager.getPhase() );
    manager.retreat();
    assertEquals( 0, manager.getPhase() );
    manager.setPhase( 5 );
    assertEquals( 5, manager.getPhase() );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testAddLifecycleListener() throws Exception {
    IPhasedLifecycleListener<TestEvent> listener = mock( IPhasedLifecycleListener.class );
    manager.addLifecycleListener( listener );
    assertEquals( 1, manager.getListenerCount() );
    manager.removeLifecycleListener( listener );
    assertEquals( 0, manager.getListenerCount() );
  }

  @Test
  public void testAdvanceAndWait() throws Exception {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final IPhasedLifecycleListener<TestEvent> listener = new LatchWaitingPhaseListener( latch );

    final AtomicBoolean completed = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {

        manager.addLifecycleListener( listener );
        try {
          manager.advanceAndWait();
          completed.set( true );
        } catch ( InterruptedException e ) {
          fail( "error with wait" );
        }
      }
    } );
    t1.start();
    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t1.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertTrue( t1.getState() == Thread.State.WAITING );
    latch.countDown();
    t1.join();
    assertTrue( completed.get() );
    assertEquals( 1, manager.getPhase() );

  }

  @Test
  public void testRetreatAndWait() throws Exception {

    final CountDownLatch latch = new CountDownLatch( 1 );
    final IPhasedLifecycleListener<TestEvent> listener = new LatchWaitingPhaseListener( latch );

    final AtomicBoolean completed = new AtomicBoolean( false );
    manager.setPhase( 1 );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {

        manager.addLifecycleListener( listener );
        try {
          manager.retreatAndWait();
          completed.set( true );
        } catch ( InterruptedException e ) {
          fail( "error with wait" );
        }
      }
    } );
    t1.start();
    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t1.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertTrue( t1.getState() == Thread.State.WAITING );
    latch.countDown();
    t1.join();
    assertTrue( completed.get() );
    assertEquals( 0, manager.getPhase() );
    manager.retreatAndWait();
    assertEquals( 0, manager.getPhase() );

  }

  @Test
  public void testSetPhaseAndWait() throws Exception {


    final CountDownLatch latch = new CountDownLatch( 1 );
    final IPhasedLifecycleListener<TestEvent> listener = new LatchWaitingPhaseListener( latch );

    final AtomicBoolean completed = new AtomicBoolean( false );
    manager.setPhase( 1 );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {

        manager.addLifecycleListener( listener );
        try {
          manager.setPhaseAndWait( 3 );
          completed.set( true );
        } catch ( InterruptedException e ) {
          fail( "error with wait" );
        }
      }
    } );
    t1.start();
    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t1.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertTrue( t1.getState() == Thread.State.WAITING );
    latch.countDown();
    t1.join();
    assertTrue( completed.get() );
    assertEquals( 3, manager.getPhase() );
  }

  @Test
  public void testAdvance() throws Exception {
    IPhasedLifecycleListener listener = mock( IPhasedLifecycleListener.class );
    manager.addLifecycleListener( listener );
    manager.advance();


    ArgumentCaptor<IPhasedLifecycleEvent> event = ArgumentCaptor.forClass( IPhasedLifecycleEvent.class );
    verify( listener ).onPhaseChange( event.capture() );
    assertEquals( 1, event.getValue().getPhase() );
    assertEquals( DUMMY_EVENT, event.getValue().getNotificationObject() );
  }

  @Test
  public void testRetreat() throws Exception {
    IPhasedLifecycleListener listener = mock( IPhasedLifecycleListener.class );
    manager.setPhase( 3 );
    manager.addLifecycleListener( listener );
    manager.retreat();

    ArgumentCaptor<IPhasedLifecycleEvent> event = ArgumentCaptor.forClass( IPhasedLifecycleEvent.class );
    verify( listener ).onPhaseChange( event.capture() );
    assertEquals( 2, event.getValue().getPhase() );
    event.getValue().accept();
    manager.setPhase( 0 );
    verify( listener, times(2) ).onPhaseChange( event.capture() );
    event.getValue().accept();
    manager.retreat();
    assertEquals( 0, manager.getPhase() );

  }

  @Test
  public void testTerminate() throws Exception {


    final CountDownLatch latch = new CountDownLatch( 1 );
    final IPhasedLifecycleListener<TestEvent> listener = mock( IPhasedLifecycleListener.class );

    final AtomicBoolean interrupted = new AtomicBoolean( false );
    manager.setPhase( 1 );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {

        manager.addLifecycleListener( listener );
        try {
          manager.setPhaseAndWait( 3 );
        } catch ( InterruptedException e ) {
          interrupted.set( true );
        }
      }
    } );
    t1.start();
    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t1.getState() != Thread.State.WAITING && ++fatalCount < 20 );
    assertTrue( t1.getState() == Thread.State.WAITING );
    manager.terminate();
    t1.join();
    assertTrue( interrupted.get() );
    assertTrue( manager.isTerminated() );

  }

  @Test
  public void testSetExecutor() throws Exception {
    Executor executor = mock( Executor.class );
    final IPhasedLifecycleListener<TestEvent> listener = mock( IPhasedLifecycleListener.class );
    manager.setExecutor( executor );
    manager.addLifecycleListener( listener );
    manager.advance();

    ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass( Runnable.class );
    verify( executor ).execute( runnable.capture() );

  }

  @Test
  public void testBlockingOperations() throws Exception {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final IPhasedLifecycleListener<TestEvent> listener = new LatchWaitingPhaseListener( latch );

    final AtomicBoolean completed = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        manager.addLifecycleListener( listener );
        try {
          manager.advanceAndWait();
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    } );
    t1.start();

    Thread t2 = new Thread( new Runnable() {
      @Override public void run() {
        try {
          manager.advance();
        } catch ( InterruptedException e ) {
          fail( "Interrupted" );
        }
        completed.set( true );
      }
    } );
    t2.start();

    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t1.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertFalse( completed.get() );

    latch.countDown();
    t2.join();
    assertTrue( completed.get() );
  }

  private class LatchWaitingPhaseListener implements IPhasedLifecycleListener<TestEvent> {
    private CountDownLatch latch;

    public LatchWaitingPhaseListener(
        CountDownLatch latch ) {
      this.latch = latch;
    }

    @Override
    public void onPhaseChange( IPhasedLifecycleEvent<TestEvent> event ) {
      try {
        latch.await();
        event.accept();
      } catch ( InterruptedException e ) {
        fail( "error waiting" );
      }

    }
  }
}
