/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.platform.pdi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by nbaker on 9/8/16.
 */
public class PlatformWebResourceTest {

  PlatformWebResource webResource = new PlatformWebResource( "context", "location" );

  @Test
  public void getContext() throws Exception {
    assertEquals( "context", webResource.getContext() );
  }

  @Test
  public void getLocation() throws Exception {
    assertEquals( "location", webResource.getLocation() );
  }

  @Test
  public void testEquals() throws Exception {
    assertEquals( webResource, new PlatformWebResource( "context", "location" ) );
    assertEquals( webResource, webResource );
  }

  @Test
  public void testNotEquals() throws Exception {
    assertNotEquals( webResource, new PlatformWebResource( "context", "blah" ) );
    assertNotEquals( webResource, null );
  }

}