/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.plugin.deployer;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.impl.BundleStateManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by bryan on 8/29/14.
 */
public class PlatformPluginMavenURLHandler extends AbstractURLStreamHandlerService {
  private List<PluginFileHandler> pluginFileHandlers;
  private BundleStateManager bundleStateManager;

  public void setPluginFileHandlers( List<PluginFileHandler> pluginFileHandlers ) {
    this.pluginFileHandlers = pluginFileHandlers;
  }
  public void setBundleStateManager( BundleStateManager bundleStateManager ) {
    this.bundleStateManager = bundleStateManager;
  }

  @Override public URLConnection openConnection( URL u ) throws IOException {
    URL mvnUrl = new URL( "mvn", null, u.getPath() );
    return new PlatformPluginBundlingURLConnection( mvnUrl, pluginFileHandlers, bundleStateManager );
  }
}
