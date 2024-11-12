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

package org.pentaho.webpackage.deployer.archive.impl;

import org.osgi.service.url.AbstractURLStreamHandlerService;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class UrlHandler extends AbstractURLStreamHandlerService {
  @Override public URLConnection openConnection( URL url ) throws IOException {
    return new WebPackageURLConnection( new URL( url.getPath() ) );
  }
}
