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
