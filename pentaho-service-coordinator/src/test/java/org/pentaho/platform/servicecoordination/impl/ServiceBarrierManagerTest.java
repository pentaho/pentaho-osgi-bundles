package org.pentaho.platform.servicecoordination.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;

import static org.junit.Assert.*;

public class ServiceBarrierManagerTest {
  ServiceBarrierManager manager;
  IServiceBarrier barrier1;
  IServiceBarrier barrier2;

  @Before
  public void setup() {
    manager = new ServiceBarrierManager();
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

}
