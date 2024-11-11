/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.pdi;

import org.junit.Test;
import org.osgi.framework.Bundle;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by nbaker on 8/16/16.
 */
public class BundleClassloaderTest {
  @Test
  public void getName() throws Exception {
    BundleClassloader bundleClassloader = new BundleClassloader( mock( Bundle.class ), "test" );
    assertEquals( "test", bundleClassloader.getName() );
    URL url = bundleClassloader.findResource( "OSGI-INF/blueprint/blueprint.xml" );
    assertNull( url );
  }

}