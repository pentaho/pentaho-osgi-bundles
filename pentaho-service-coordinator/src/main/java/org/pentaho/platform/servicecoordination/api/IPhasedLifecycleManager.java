package org.pentaho.platform.servicecoordination.api;

import java.util.concurrent.Executor;

/**
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleManager<T> {
  int getPhase();
  void addLifecycleListener( IPhasedLifecycleListener<T> listener );
  void removeLifecycleListener( IPhasedLifecycleListener<T> listener );
  int getListenerCount();
  int advanceAndWait() throws InterruptedException;
  int retreatAndWait() throws InterruptedException;
  void setPhaseAndWait( int phase ) throws InterruptedException;
  int advance() throws InterruptedException;
  int retreat() throws InterruptedException;
  void setPhase( int phase );
  void setExecutor( Executor executorService );
  void terminate();
  boolean isTerminated();
}
