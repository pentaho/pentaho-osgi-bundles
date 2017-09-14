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
