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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;


public class ServiceBarrierManagerTest {
  private static final String MANAGER_CLASS = "org.pentaho.platform.api.engine.IServiceBarrierManager.class";

  IServiceBarrierManager manager;
  IServiceBarrier barrier1;
  IServiceBarrier barrier2;

  @Before
  public void setup() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.clearProperty( MANAGER_CLASS );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    barrier1 = manager.getServiceBarrier( "1" );
    barrier2 = manager.getServiceBarrier( "2" );
  }

  @Test
  public void testGetServiceBarrier() throws Exception {
    IServiceBarrier barrier1a = manager.getServiceBarrier( "1" );

    assertSame( barrier1, barrier1a );
    assertNotSame( barrier1, barrier2 );
  }

  @Test
  public void testGetAllServiceBarriers() throws Exception {
    assertEquals( 2, manager.getAllServiceBarriers().size() );
    assertTrue( manager.getAllServiceBarriers().contains( barrier1 ) );
    assertTrue( manager.getAllServiceBarriers().contains( barrier2 ) );
  }

  @Test
  public void testExceptions() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.setProperty( MANAGER_CLASS, "foo" );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    assertNotNull( manager );
    assertTrue( manager instanceof ServiceBarrierManager );
  }

  @Test
  public void testPluggableClass() {
    IServiceBarrierManager.LOCATOR.instance = null;
    System.setProperty( MANAGER_CLASS, MockPluggedInManager.class.getName() );
    manager = IServiceBarrierManager.LOCATOR.getManager();
    assertNotNull( manager );
    assertTrue( manager instanceof MockPluggedInManager );
  }

  public static class MockPluggedInManager implements IServiceBarrierManager {

    @Override
    public IServiceBarrier getServiceBarrier( String serviceID ) {
      return null;
    }

    @Override
    public List<IServiceBarrier> getAllServiceBarriers() {
      return null;
    }

  }

}
