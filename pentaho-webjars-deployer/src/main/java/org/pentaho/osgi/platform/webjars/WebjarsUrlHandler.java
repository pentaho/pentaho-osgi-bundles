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

package org.pentaho.osgi.platform.webjars;

import org.osgi.service.url.AbstractURLStreamHandlerService;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by nbaker on 9/6/14.
 */
public class WebjarsUrlHandler extends AbstractURLStreamHandlerService {
  private final boolean automaticNonAmdShimConfigEnabled;

  public WebjarsUrlHandler( boolean automaticNonAmdShimConfigEnabled ) {
    this.automaticNonAmdShimConfigEnabled = automaticNonAmdShimConfigEnabled;
  }

  @Override public URLConnection openConnection( URL url ) throws IOException {
    return new WebjarsURLConnection( new URL( url.getPath() ), this.automaticNonAmdShimConfigEnabled );
  }
}
