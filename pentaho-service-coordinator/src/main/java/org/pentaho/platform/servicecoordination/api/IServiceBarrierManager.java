package org.pentaho.platform.servicecoordination.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by nbaker on 1/26/15.
 */
public interface IServiceBarrierManager {

  IServiceBarrier getServiceBarrier( String serviceID );

}
