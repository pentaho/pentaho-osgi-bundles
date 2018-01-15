/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.platform.servicecoordination.impl;

import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Lifecycle event operating on a CountDownLatch.
 * <p/>
 * Created by nbaker on 2/5/15.
 */
public class CountdownLatchLifecycleEvent<T> implements IPhasedLifecycleEvent<T> {

  private int phase;
  private T notificationObject;
  private CountDownLatch latch;
  private IPhasedLifecycleManager<T> manager;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public CountdownLatchLifecycleEvent( int phase, T notificationObject, CountDownLatch latch,
                                       IPhasedLifecycleManager<T> manager ) {
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
