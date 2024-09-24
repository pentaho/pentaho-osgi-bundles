/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.platform.servicecoordination.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;

/**
 * 
 * @author tkafalas
 *
 */
public class ServiceBarrierThreadTest {
  final static String BARRIER_ID = "JCRBarrier";

  /**
   * Test simulates 3 features waiting for the main thread to release the barrier before continuing.
   * 
   * @throws Exception
   */
  @Test
  public void testMultipleThreads1() throws Exception {

    final ServiceBarrierManager mngr = new ServiceBarrierManager();
    IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
    assertTrue( barrier.isAvailable() );

    // barrier.hold();
    barrier.hold();

    Thread t1 = new Thread( new ServiceTestThread( mngr, 1 ) );
    Thread t2 = new Thread( new ServiceTestThread( mngr, 2 ) );
    Thread t3 = new Thread( new ServiceTestThread( mngr, 3 ) );
    t1.start();
    t2.start();
    t3.start();

    Thread.sleep( 3000 );
    // barrier.release();
    assertTrue( t1.getState() == Thread.State.WAITING );
    assertTrue( t2.getState() == Thread.State.WAITING );
    assertTrue( t3.getState() == Thread.State.WAITING );
    barrier.release();

    while ( t1.isAlive() || t2.isAlive() || t3.isAlive() ) {
      Thread.sleep( 10 );
    }

    System.out.println( "barrier availability test 1 is " + barrier.isAvailable() + " "  + barrier.getHoldCount() );
  }

  /**
   * Test simulates 3 features waiting for the main thread to release the barrier before continuing. The main thread
   * terminates the 3 waiting threads, presumably due to fatal error.
   * 
   * @throws Exception
   */
  @Test
  public void testMultipleThreads2() throws Exception {

    final ServiceBarrierManager mngr = new ServiceBarrierManager();
    IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
    assertTrue( barrier.isAvailable() );

    barrier.hold();

    Thread t1 = new Thread( new ServiceTestThread( mngr, 1 ) );
    Thread t2 = new Thread( new ServiceTestThread( mngr, 2 ) );
    Thread t3 = new Thread( new ServiceTestThread( mngr, 3 ) );
    t1.start();
    t2.start();
    t3.start();

    for ( int i = 0; i < 10; i++ ) {
      if ( t1.getState() == Thread.State.WAITING && t2.getState() == Thread.State.WAITING
          && t3.getState() == Thread.State.WAITING ) {
        break;
      }
      Thread.sleep( 50 );
    }
    assertTrue( t1.getState() == Thread.State.WAITING );
    assertTrue( t2.getState() == Thread.State.WAITING );
    assertTrue( t3.getState() == Thread.State.WAITING );
    barrier.terminate();
    
    t1.join();
    t2.join();
    t3.join();

    assertTrue( barrier.isTerminated() );
    System.out.println( "barrier availability test 2 is " + barrier.isAvailable() + " "  + barrier.getHoldCount() );
  }

  public static class ServiceTestThread implements Runnable {
    private int threadNum;
    ServiceBarrierManager mngr;

    public ServiceTestThread( ServiceBarrierManager mngr, int threadNum ) {
      this.mngr = mngr;
      this.threadNum = threadNum;
    }

    @Override
    public void run() {
      IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
      assertFalse( barrier.isAvailable() );
      try {
        System.out.println( "Thread " + threadNum + " waiting." );
        barrier.awaitAvailability();
        System.out.println( "Thread " + threadNum + " running" );
      } catch ( InterruptedException e ) {
        System.out.println( "Thread " + threadNum + " terminated" );
        e.printStackTrace();
        assertTrue( barrier.isTerminated() );
      } finally {
        barrier.release();
      }
    }
  }

}
