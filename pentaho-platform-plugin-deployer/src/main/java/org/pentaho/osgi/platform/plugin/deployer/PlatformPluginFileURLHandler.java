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
public class PlatformPluginFileURLHandler extends AbstractURLStreamHandlerService {
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

