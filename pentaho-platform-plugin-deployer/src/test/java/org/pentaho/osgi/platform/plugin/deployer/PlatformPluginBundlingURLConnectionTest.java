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
