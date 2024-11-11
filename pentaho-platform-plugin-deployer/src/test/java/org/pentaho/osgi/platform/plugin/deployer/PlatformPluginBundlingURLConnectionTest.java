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

package org.pentaho.osgi.platform.plugin.deployer;

import org.junit.Test;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 8/28/14.
 */
public class PlatformPluginBundlingURLConnectionTest {
  @Test
  public void testGetMaxSizeReturnsTenMegsByDefault() {
    assertEquals( PlatformPluginBundlingURLConnection.TEN_MEGABYTES, PlatformPluginBundlingURLConnection.getMaxSize( null ) );
    assertEquals( PlatformPluginBundlingURLConnection.TEN_MEGABYTES, PlatformPluginBundlingURLConnection.getMaxSize( "" ) );
  }

  @Test
  public void testGetMaxSizeReturnsMaxSize() {
    assertEquals( 101, PlatformPluginBundlingURLConnection.getMaxSize( "maxSize=101" ) );
  }

  @Test
  public void testConnectNoop() throws IOException {
    new PlatformPluginBundlingURLConnection( new URL( "http://www.pentaho.com" ), new ArrayList<PluginFileHandler>(  ) ).connect();
  }
}
