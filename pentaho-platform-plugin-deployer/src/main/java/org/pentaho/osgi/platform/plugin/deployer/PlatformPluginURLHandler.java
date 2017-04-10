/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.BundleStateManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by bryan on 8/26/14.
 */
public class PlatformPluginURLHandler extends AbstractURLStreamHandlerService {
  private List<PluginFileHandler> pluginFileHandlers;
  private BundleStateManager bundleStateManager;

  public void setPluginFileHandlers( List<PluginFileHandler> pluginFileHandlers ) {
    this.pluginFileHandlers = pluginFileHandlers;
  }

  public void setBundleStateManager( BundleStateManager bundleStateManager ) {
    this.bundleStateManager = bundleStateManager;
  }

  @Override public URLConnection openConnection( URL u ) throws IOException {
    URL fileUrl = new URL( "file", null, u.getPath() );
    return new PlatformPluginBundlingURLConnection( fileUrl, pluginFileHandlers );
  }
}

