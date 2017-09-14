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

import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Base Implementation of the IPhasedLifecycleManager based on a CoundDownLatch mechanism. A Synchronous executor will
 * be used if one is not supplied. Subclasses must implement the getNotificationObject() method.
 * <p/>
 * Created by nbaker on 2/5/15.
 */
public abstract class BaseCountdownLatchLifecycleManager<T> implements IPhasedLifecycleManager<T> {

  private int phase;
  private List<IPhasedLifecycleListener<T>> listeners = new ArrayList<IPhasedLifecycleListener<T>>();
  private CountDownLatch latch = new CountDownLatch( 0 );
  private Thread lockingThread;
  private Executor executorService;
  private boolean terminated;

  public BaseCountdownLatchLifecycleManager() {

  }

  public BaseCountdownLatchLifecycleManager(
      List<IPhasedLifecycleListener<T>> listeners ) {
    this.listeners = listeners;
  }

  @Override public int getPhase() {
    return phase;
  }

  @Override public void addLifecycleListener( IPhasedLifecycleListener<T> listener ) {
    checkTerminated();
    listeners.add( listener );
  }

  @Override public void removeLifecycleListener( IPhasedLifecycleListener<T> listener ) {
    checkTerminated();
    listeners.remove( listener );
  }

  @Override public int getListenerCount() {
    checkTerminated();
    return listeners.size();
  }

  @Override public synchronized int advanceAndWait() throws InterruptedException {
    checkTerminated();
    latch.await();
    phase++;
    notifyListenersAndWait();
    return phase;
  }

  @Override public synchronized int retreatAndWait() throws InterruptedException {
    checkTerminated();
    latch.await();
    if ( phase - 1 < 0 ) {
      return 0;
    }
    phase--;
    notifyListenersAndWait();
    return phase;
  }

  @Override public synchronized void setPhaseAndWait( int phase ) throws InterruptedException {
    checkTerminated();
    latch.await();
    this.phase = phase;
    notifyListenersAndWait();
  }

  @Override public synchronized int advance() throws InterruptedException {
    checkTerminated();
    latch.await();
    phase++;
    notifyListeners();
    return phase;
  }

  @Override public synchronized int retreat() throws InterruptedException {
    checkTerminated();
    latch.await();
    if ( phase - 1 < 0 ) {
      return 0;
    }
    phase--;
    notifyListeners();
    return phase;
  }

  @Override public void terminate() {
    checkTerminated();
    this.terminated = true;
    try {
      if ( lockingThread != null ) {
        lockingThread.interrupt();
      }
    } finally {
      lockingThread = null;
    }
  }

  @Override public boolean isTerminated() {
    return terminated;
  }

  private void notifyListenersAndWait() throws InterruptedException {

    try {
      lockingThread = Thread.currentThread();
      notifyListeners();
      latch.await();
    } finally {
      lockingThread = null;
    }
  }

  private void notifyListeners() {
    latch = new CountDownLatch( listeners.size() );

    for ( IPhasedLifecycleListener<T> listener : listeners ) {
      IPhasedLifecycleEvent<T> event =
          new CountdownLatchLifecycleEvent<T>( phase, getNotificationObject(), latch, this );
      getExecutor().execute( new EventRunnable<T>( listener, event ) );
    }
  }

  /**
   * Implementors need to return a notification object for the current phase.
   *
   * @return
   */
  protected abstract T getNotificationObject();

  @Override public synchronized void setPhase( int phase ) {
    checkTerminated();
    this.phase = phase;
    notifyListeners();

  }

  @Override public void setExecutor( Executor executorService ) {
    checkTerminated();
    this.executorService = executorService;
  }

  public Executor getExecutor() {
    if ( executorService == null ) {
      executorService = new SynchronousExecutor();
    }
    return executorService;
  }

  private static class EventRunnable<T> implements Runnable {
    IPhasedLifecycleListener<T> listener;
    IPhasedLifecycleEvent<T> event;

    public EventRunnable( IPhasedLifecycleListener<T> listener,
                          IPhasedLifecycleEvent<T> event ) {
      this.listener = listener;
      this.event = event;
    }

    @Override public void run() {
      listener.onPhaseChange( event );
    }
  }

  private static class SynchronousExecutor implements Executor {
    @Override public void execute( Runnable command ) {
      command.run();
    }
  }

  private void checkTerminated() {
    if ( terminated ) {
      throw new IllegalStateException( "Manager has been terminated" );
    }
  }
}
