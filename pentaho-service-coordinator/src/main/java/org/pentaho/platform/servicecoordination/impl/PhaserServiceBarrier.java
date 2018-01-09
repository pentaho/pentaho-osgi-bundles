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

import org.pentaho.platform.servicecoordination.api.IServiceBarrier;

import java.util.concurrent.Phaser;

/**
 * Created by nbaker on 1/29/15.
 */
public class PhaserServiceBarrier implements IServiceBarrier {
  private final Phaser phaser = new Phaser();
  private String serviceId;

  public PhaserServiceBarrier( ) {

  }

  public PhaserServiceBarrier( String serviceId ) {
    this.serviceId = serviceId;
  }

  @Override public int hold() {
    return phaser.register();
  }

  @Override public int release() {
    return phaser.arriveAndDeregister();
  }

  @Override public int getHoldCount() {
    return phaser.getUnarrivedParties();
  }

  @Override public boolean isAvailable() {
    return phaser.getUnarrivedParties() <= 0;
  }

  @Override public int awaitAvailability() throws InterruptedException {
    if ( isAvailable() ) {
      return phaser.getPhase();
    }
    phaser.register();
    phaser.arrive();
    int phase = phaser.awaitAdvanceInterruptibly( phaser.getPhase() );
    if ( phaser.isTerminated() ) {
      throw new InterruptedException();
    }
    return phase;
  }

  @Override public void terminate() {
    phaser.forceTermination();
  }

  @Override public boolean isTerminated() {
    return phaser.isTerminated();
  }
}
