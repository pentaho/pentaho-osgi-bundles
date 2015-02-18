package org.pentaho.platform.servicecoordination.impl;

import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by nbaker on 2/5/15.
 */
public class CountdownLatchLifecycleEvent<T> implements IPhasedLifecycleEvent<T> {

  private int phase;
  private T notificationObject;
  private CountDownLatch latch;
  private IPhasedLifecycleManager<T> manager;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public CountdownLatchLifecycleEvent( int phase, T notificationObject, CountDownLatch latch, IPhasedLifecycleManager<T> manager ) {
    this.phase = phase;
    this.notificationObject = notificationObject;
    this.latch = latch;
    this.manager = manager;
  }

  @Override public int getPhase() {
    return phase;
  }

  @Override public T getNotificationObject() {
    return notificationObject;
  }

  @Override public void accept() {
    latch.countDown();
  }

  @Override public void exception( Throwable t ) {
    logger.error( "Error processing Lifecycle Event", t );
    manager.terminate();
  }
}
