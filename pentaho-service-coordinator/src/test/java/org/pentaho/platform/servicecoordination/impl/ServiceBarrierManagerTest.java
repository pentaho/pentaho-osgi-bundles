package org.pentaho.platform.servicecoordination.impl;

import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;

import static org.junit.Assert.*;

public class ServiceBarrierManagerTest {

  @Test
  public void testGetServiceBarrier() throws Exception {
    ServiceBarrierManager manager = new ServiceBarrierManager();
    IServiceBarrier barrier1 = manager.getServiceBarrier( "1" );
    IServiceBarrier barrier1a = manager.getServiceBarrier( "1" );
    IServiceBarrier barrier2 = manager.getServiceBarrier( "2" );

    assertSame( barrier1, barrier1a );
    assertNotSame( barrier1, barrier2);
  }
}