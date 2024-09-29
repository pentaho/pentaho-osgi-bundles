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

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by bryan on 8/26/14.
 */
public class PlatformPluginFileURLHandlerTest {
  @Test
  public void testUrlHandler() throws IOException {
    PlatformPluginFileURLHandler platformPluginURLHandler = new PlatformPluginFileURLHandler();
    assertTrue( platformPluginURLHandler.openConnection( new URL( "http://www.pentaho.com" ) ) instanceof  PlatformPluginBundlingURLConnection );
  }
}
