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

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;

/**
 * Created by nbaker on 2/20/15.
 */
public class ServiceBarrierManager implements IServiceBarrierManager {
  WeakHashMap<String, IServiceBarrier> barriers = new WeakHashMap<String, IServiceBarrier>();

  @Override
  public IServiceBarrier getServiceBarrier( String serviceID ) {
    if ( barriers.containsKey( serviceID ) ) {
      return barriers.get( serviceID );
    }
    PhaserServiceBarrier phaserServiceBarrier = new PhaserServiceBarrier( serviceID );
    barriers.put( serviceID, phaserServiceBarrier );
    return phaserServiceBarrier;
  }

  @Override
  public List<IServiceBarrier> getAllServiceBarriers() {
    return new ArrayList<IServiceBarrier>( barriers.values() );
  }

}
