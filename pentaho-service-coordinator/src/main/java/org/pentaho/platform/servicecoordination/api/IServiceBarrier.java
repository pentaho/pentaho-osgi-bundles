package org.pentaho.platform.servicecoordination.api;

import java.util.List;

/**
 * Created by nbaker on 1/27/15.
 */
public interface IServiceBarrier {
  int hold();
  int release();
  int getHoldCount();
  boolean isAvailable();
  int awaitAvailability() throws InterruptedException;
  void terminate();
  boolean isTerminated();
}
